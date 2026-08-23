import { WebSocketServer, WebSocket } from 'ws';
import type { Server } from 'node:http';
import { verifyToken } from '../lib/jwt.js';

const clients = new Map<string, Set<WebSocket>>();

/** 向指定用户的所有连接推送消息 (订单状态/骑手位置等) */
export function pushToUser(userId: string, payload: Record<string, unknown>) {
  const set = clients.get(userId);
  if (!set) return;
  const data = JSON.stringify(payload);
  for (const ws of set) {
    if (ws.readyState === WebSocket.OPEN) ws.send(data);
  }
}

/**
 * 挂载 WebSocket 服务, 路径 /ws?token=JWT。
 * 用户端订阅订单实时状态; 骑手端上报 GPS 亦可复用此通道 (生产按角色鉴权)。
 */
export function attachRealtime(server: Server) {
  const wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', (ws, req) => {
    try {
      const url = new URL(req.url ?? '', 'http://localhost');
      const token = url.searchParams.get('token');
      if (!token) return ws.close(4001, 'missing token');
      const { sub: userId } = verifyToken(token);

      if (!clients.has(userId)) clients.set(userId, new Set());
      clients.get(userId)!.add(ws);

      ws.send(JSON.stringify({ event: 'connected', userId }));

      ws.on('close', () => {
        clients.get(userId)?.delete(ws);
        if (clients.get(userId)?.size === 0) clients.delete(userId);
      });
    } catch {
      ws.close(4003, 'invalid token');
    }
  });

  return wss;
}
