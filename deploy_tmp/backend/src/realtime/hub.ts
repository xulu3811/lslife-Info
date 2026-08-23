import { WebSocketServer, WebSocket } from 'ws';
import type { Server } from 'node:http';
import { verifyToken } from '../lib/jwt.js';
import { prisma } from '../lib/prisma.js';
import { encryptChatMessage, decryptChatMessage, generateEvidenceHash } from '../lib/crypto.js';

interface ExtWebSocket extends WebSocket {
  isAlive?: boolean;
  userId?: string;
}

const clients = new Map<string, Set<ExtWebSocket>>();
const rooms = new Map<string, Set<ExtWebSocket>>();

/** 向指定用户的所有连接推送消息 (订单状态/聊天消息等) */
export function pushToUser(userId: string, payload: Record<string, unknown>) {
  const set = clients.get(userId);
  if (!set) return;
  const data = JSON.stringify(payload);
  for (const ws of set) {
    if (ws.readyState === WebSocket.OPEN) ws.send(data);
  }
}

/** 向订阅某房间的所有连接推送消息 */
export function pushToRoom(room: string, payload: Record<string, unknown>) {
  const set = rooms.get(room);
  if (!set) return;
  const data = JSON.stringify(payload);
  for (const ws of set) {
    if (ws.readyState === WebSocket.OPEN) ws.send(data);
  }
}

/**
 * 挂载 WebSocket 服务, 路径 /ws?token=JWT。
 * 支持 15s 心跳保活、断线重连补发、AES-256-GCM 终身加密落盘与防篡改存证哈希链
 */
export function attachRealtime(server: Server) {
  const wss = new WebSocketServer({ 
    server, 
    path: '/ws',
    maxPayload: 50 * 1024 * 1024 
  });

  // 30秒周期心跳保活检测
  const pingInterval = setInterval(() => {
    wss.clients.forEach((ws: WebSocket) => {
       const extWs = ws as ExtWebSocket;
       if (extWs.isAlive === false) {
          console.log('[WS Hub] Terminating inactive half-open socket for user:', extWs.userId);
          return extWs.terminate();
       }
       extWs.isAlive = false;
       extWs.ping();
    });
  }, 30_000);

  wss.on('close', () => {
    clearInterval(pingInterval);
  });

  wss.on('connection', (ws: ExtWebSocket, req) => {
    try {
      const url = new URL(req.url ?? '', 'http://localhost');
      const token = url.searchParams.get('token');
      if (!token) return ws.close(4001, 'missing token');
      const { sub: userId } = verifyToken(token);

      ws.isAlive = true;
      ws.userId = userId;

      if (!clients.has(userId)) clients.set(userId, new Set());
      clients.get(userId)!.add(ws);

      ws.send(JSON.stringify({ event: 'connected', userId, timestamp: Date.now() }));

      ws.on('pong', () => {
        ws.isAlive = true;
      });
      ws.on('ping', () => {
        ws.isAlive = true;
      });

      ws.on('message', async (data) => {
        try {
          const msg = JSON.parse(data.toString());
          
          // 1. 应用层心跳探活应答 (增强兼容性)
          if (msg.action === 'ping') {
             ws.isAlive = true;
             ws.send(JSON.stringify({ event: 'pong', timestamp: Date.now() }));
             return;
          }

          // 2. 房间订阅 (如 order:xxx)
          if (msg.action === 'subscribe' && msg.room) {
             const room = msg.room as string;
             if (!rooms.has(room)) rooms.set(room, new Set());
             rooms.get(room)!.add(ws);
             ws.send(JSON.stringify({ event: 'subscribed', room }));
             return;
          }

          // 3. 弱网重连增量同步补送 (Sync Offline Messages)
          if (msg.action === 'sync_offline') {
             const conversationId = msg.conversationId as string | undefined;
             // 找出需要同步的会话（有未读 OR 指定会话）
             const targetConvs = conversationId && conversationId !== 'all'
                 ? [{ id: conversationId }]
                 : await prisma.chatSession.findMany({
                     where: {
                       OR: [
                         { user1Id: userId },
                         { user2Id: userId }
                       ]
                     },
                     select: { id: true }
                   });

             for (const c of targetConvs) {
               // 双向同步：包含自己和对方发的消息，取最近 50 条
               const msgs = await prisma.chatMessage.findMany({
                 where: { sessionId: c.id },
                 orderBy: { createdAt: 'desc' },
                 take: 50
               });
               if (msgs.length > 0) {
                 msgs.reverse(); // 恢复升序
                 const decryptedMsgs = msgs.map(m => ({
                   ...m,
                   content: (m.isEncrypted && !m.isRecalled) ? decryptChatMessage(m.content) : m.content,
                   msgType: m.type   // 确保字段名与客户端一致
                 }));
                 ws.send(JSON.stringify({ event: 'offline_sync', conversationId: c.id, messages: decryptedMsgs }));
               }
             }
             return;
          }

          // 4. 发送实时消息 (自动加解密 + SHA-256 哈希存证链 + 原子事务)
          if (msg.action === 'chat') {
             console.log(`\n[DEBUG-TIME] chat action received at: ${Date.now()} for clientMsgId: ${msg.clientMsgId}`);
             const clientMsgId = msg.clientMsgId || require('crypto').randomUUID();
             const toUserId = msg.toUserId;
             const content = msg.content;
             const type = msg.type || 'TEXT';
             const mediaHash = msg.mediaHash || null;
             
             if (!toUserId || !content) return;

             if (userId === toUserId) {
                 ws.send(JSON.stringify({ event: 'error', message: '不能给自己发送消息' }));
                 return;
             }

             // 4.1 寻找或创建会话 (user1Id 必须恒定为较小的一方，以保证唯一索引命中)
             const user1Id = userId < toUserId ? userId : toUserId;
             const user2Id = userId < toUserId ? toUserId : userId;
             
             let conv = await prisma.chatSession.findFirst({
                 where: { user1Id, user2Id }
             });

             if (!conv) {
                 conv = await prisma.chatSession.create({
                     data: { user1Id, user2Id, lastMessage: '' }
                 });
             }

             // 4.2 获取该会话上一条消息的防篡改证据哈希
             const prevMsg = await prisma.chatMessage.findFirst({
                 where: { sessionId: conv.id },
                 orderBy: { createdAt: 'desc' },
                 select: { evidenceHash: true }
             });

             const now = new Date();
             // 4.3 生成 SHA-256 密码学防篡改存证链哈希
             const evidenceHash = generateEvidenceHash(conv.id, userId, now, content, prevMsg?.evidenceHash, mediaHash);
             // 4.4 AES-256-GCM 高强度应用层加密落盘
             const encryptedContent = encryptChatMessage(content);

             // 4.5 判断未读数增加给谁
             const isUser1 = userId === conv.user1Id;

             // 4.6 事务写入
             const [savedMessage, updatedConv] = await prisma.$transaction([
                 prisma.chatMessage.create({
                     data: {
                         id: clientMsgId,
                         sessionId: conv.id,
                         senderId: userId,
                         type: type,
                         content: encryptedContent,
                         evidenceHash: evidenceHash,
                         createdAt: now
                     }
                 }),
                 prisma.chatSession.update({
                     where: { id: conv.id },
                     data: {
                         lastMessage: encryptedContent,
                         unread1: !isUser1 ? { increment: 1 } : undefined,
                         unread2: isUser1 ? { increment: 1 } : undefined
                     }
                 })
             ]);
             console.log(`[DEBUG-TIME] DB Transaction completed at: ${Date.now()}`);

             // 4.5 回执 ACK 给发送方 (更新本地为 SENT)
             ws.send(JSON.stringify({ 
                 event: 'MSG_ACK', 
                 clientMsgId,
                 serverMsgId: savedMessage.id,
                 conversationId: conv.id,
                 status: 'SENT'
             }));
             console.log(`[DEBUG-TIME] MSG_ACK sent to sender at: ${Date.now()}`);

             // 4.6 实时推送给接收方 (WebSocket)
             const payload = {
                 event: 'chat_message',
                 message: {
                    id: savedMessage.id,
                    sessionId: savedMessage.sessionId,
                    senderId: savedMessage.senderId,
                    type: savedMessage.type,
                    content: msg.content,
                    createdAt: savedMessage.createdAt
                 }
             };
             pushToUser(toUserId, payload);
             console.log(`[DEBUG-TIME] pushToUser completed at: ${Date.now()}`);
             return;
          }

          // 5. 已读回执 (Read ACK) - 清零未读计数
          if (msg.action === 'read_ack' && msg.sessionId) {
             const sessionId = msg.sessionId as string;
             const conv = await prisma.chatSession.findUnique({ where: { id: sessionId } });
             if (conv && (conv.user1Id === userId || conv.user2Id === userId)) {
                const isUser1 = conv.user1Id === userId;
                await prisma.chatSession.update({
                   where: { id: sessionId },
                   data: {
                      unread1: isUser1 ? 0 : undefined,
                      unread2: !isUser1 ? 0 : undefined
                   }
                });
             }
             return;
          }

          // 6. 消息撤回指令 (因 Message 模型已重构，暂屏蔽撤回，以后添加)
          if (msg.action === 'recall') {
             // 预留接口，后续按需实现
             return;
          }
        } catch (e) {
          console.error("WS message error", e);
        }
      });

      ws.on('close', () => {
        clients.get(userId)?.delete(ws);
        if (clients.get(userId)?.size === 0) clients.delete(userId);
        
        rooms.forEach((subs, roomName) => {
           subs.delete(ws);
           if (subs.size === 0) rooms.delete(roomName);
        });
      });
    } catch {
      ws.close(4003, 'invalid token');
    }
  });

  return wss;
}
