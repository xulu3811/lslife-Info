import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { decryptChatMessage } from '../lib/crypto.js';
import { pushToUser } from '../realtime/hub.js';

const router = Router();

// 获取当前用户的所有聊天会话列表
router.get(
  '/sessions',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    
    const sessions = await prisma.chatSession.findMany({
      where: {
        OR: [
          { user1Id: userId },
          { user2Id: userId }
        ],
        NOT: {
          deletedBy: {
            has: userId
          }
        }
      },
      orderBy: { updatedAt: 'desc' }
    });

    // 组合对方的简单用户信息
    const result = await Promise.all(sessions.map(async s => {
      const isUser1 = s.user1Id === userId;
      const otherId = isUser1 ? s.user2Id : s.user1Id;
      const other = await prisma.user.findUnique({
        where: { id: otherId },
        select: { id: true, nickname: true, avatar: true }
      });
      return {
        id: s.id,
        targetUser: other,
        lastMessage: s.lastMessage ? decryptChatMessage(s.lastMessage) : s.lastMessage,
        unread: isUser1 ? s.unread1 : s.unread2,
        updatedAt: s.updatedAt
      };
    }));

    return ok(res, result);
  })
);

// 获取某个会话的消息记录 (自动在线解密还原明文)
router.get(
  '/sessions/:id/messages',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { id } = req.params;
    
    const session = await prisma.chatSession.findUnique({ where: { id } });
    if (!session) return ok(res, []);
    if (session.user1Id !== userId && session.user2Id !== userId) {
      throw new ApiError(403, 'Forbidden');
    }

    // 清空未读数
    const isUser1 = session.user1Id === userId;
    await prisma.chatSession.update({
      where: { id },
      data: {
        unread1: isUser1 ? 0 : undefined,
        unread2: !isUser1 ? 0 : undefined
      }
    });

    const messages = await prisma.chatMessage.findMany({
      where: { sessionId: id },
      orderBy: { createdAt: 'desc' },
      take: 50
    });
    
    messages.reverse(); // 恢复为按时间升序

    // 对加密存证进行实时应用层解密渲染
    const decryptedMessages = messages.map(m => ({
      ...m,
      content: (m.isEncrypted && !m.isRecalled) ? decryptChatMessage(m.content) : m.content
    }));

    return ok(res, decryptedMessages);
  })
);

// RESTful 消息撤回接口 (1分钟时间窗口双重保障)
router.post(
  '/sessions/:id/messages/:msgId/recall',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { id, msgId } = req.params;

    const session = await prisma.chatSession.findUnique({ where: { id } });
    if (!session) throw new ApiError(404, 'Session not found');

    const msg = await prisma.chatMessage.findUnique({ where: { id: msgId } });
    if (!msg || msg.sessionId !== id) throw new ApiError(404, 'Message not found');
    if (msg.senderId !== userId) throw new ApiError(403, '只能撤回自己发送的消息');

    // 校验 60 秒时间窗口
    const elapsedMs = Date.now() - new Date(msg.createdAt).getTime();
    if (elapsedMs > 60_000) {
      throw new ApiError(400, '发送超过1分钟，无法撤回');
    }

    const recallText = '对方撤回了一条消息';
    await prisma.chatMessage.update({
      where: { id: msgId },
      data: {
        isRecalled: true,
        type: 'recalled',
        content: recallText,
        isEncrypted: false
      }
    });

    // 更新会话摘要
    await prisma.chatSession.update({
      where: { id },
      data: { lastMessage: recallText }
    });

    // 通过实时通道全网广播撤回通知
    const recallPayload = {
      event: 'message_recalled',
      messageId: msgId,
      sessionId: id,
      senderId: userId
    };
    pushToUser(session.user1Id, recallPayload);
    pushToUser(session.user2Id, recallPayload);

    return ok(res, { success: true, messageId: msgId });
  })
);

// 清空当前用户的聊天记录并彻底级联删除会话及历史消息
router.delete(
  '/sessions/:id',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const sessionId = req.params.id;
    
    const session = await prisma.chatSession.findUnique({ where: { id: sessionId } });
    if (!session) {
      return ok(res, null, '会话已删除');
    }
    if (session.user1Id !== userId && session.user2Id !== userId) {
      throw new ApiError(403, '无权删除该会话');
    }

    // 级联物理删除该会话及所有消息记录
    await prisma.chatSession.delete({
      where: { id: sessionId }
    });

    return ok(res, null, '聊天记录与会话已彻底删除');
  })
);

export default router;
