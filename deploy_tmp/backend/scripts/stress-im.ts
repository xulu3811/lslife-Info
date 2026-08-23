/**
 * IM 即时通讯 WebSocket 链路压力测试
 * 测试 AES-256-GCM 与 Prisma PostgreSQL 的并发承载力
 */
import WebSocket from 'ws';
import crypto from 'node:crypto';
import { performance } from 'node:perf_hooks';

const BASE_HTTP = process.env.BASE_HTTP ?? 'http://localhost:4000/api';
const BASE_WS = process.env.BASE_WS ?? 'ws://localhost:4000/ws';
const PAIRS = Number(process.env.PAIRS ?? 10); // 用户对数
const MESSAGES_PER_PAIR = Number(process.env.MESSAGES ?? 20); // 每对用户互发的总消息数

type Envelope<T> = { code: number; message: string; data: T };

async function call<T>(method: string, path: string, body?: unknown, token?: string): Promise<T> {
  const res = await fetch(BASE_HTTP + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const json = (await res.json()) as Envelope<T>;
  if (json.code !== 0) throw new Error(`${method} ${path}: ${json.message}`);
  return json.data;
}

// 模拟创建账户并换取 Token
async function createUserAndGetToken(i: number) {
  const phone = `138${String(10000000 + ((Date.now() + i * 11) % 89999999)).slice(0, 8)}`;
  const password = `ImTest${100000 + i}`;
  const reg = await call<{ token: string; user: { id: string } }>('POST', '/auth/register', {
    phone,
    password,
    nickname: `IM压测${i}`,
  });
  return { id: reg.user.id, token: reg.token };
}

// 封装 WebSocket 客户端连结池
class TestClient {
  ws: WebSocket;
  userId: string;
  ready: Promise<void>;
  private rttMap = new Map<string, number>();
  public latencies: number[] = [];
  public messagesReceived = 0;

  constructor(userId: string, token: string) {
    this.userId = userId;
    this.ws = new WebSocket(`${BASE_WS}?token=${token}`);
    this.ready = new Promise((resolve) => {
      this.ws.on('message', (data: Buffer) => {
        const msg = JSON.parse(data.toString());
        if (msg.event === 'connected') resolve();
        if (msg.event === 'chat_message') {
          // 如果是我们自己发出去的 Echo 或者对方发给我们的，可以计算 RTT
          // 简易做法，我们在 content 里放入发送时的时间戳
          if (msg.message.content.includes('|TS=')) {
             const tsMatches = msg.message.content.match(/\|TS=(\d+)/);
             if (tsMatches && tsMatches[1]) {
                const ts = Number(tsMatches[1]);
                this.latencies.push(Date.now() - ts);
             }
          }
          this.messagesReceived++;
        }
      });
    });
  }

  send(toUserId: string, isImage: boolean) {
    const ts = Date.now();
    let content = `压测文本|TS=${ts}`;
    let type = 'text';
    if (isImage) {
       // 模拟 100KB 左右的 Base64 图片大负载
       content = `base64:${crypto.randomBytes(75000).toString('base64')}|TS=${ts}`;
       type = 'image';
    }
    this.ws.send(JSON.stringify({
      action: 'chat',
      toUserId,
      type,
      content
    }));
  }

  close() {
    this.ws.close();
  }
}

async function main() {
  console.log(`🚀 开始 IM 压测：连接对数=${PAIRS}，总消息=${PAIRS * MESSAGES_PER_PAIR}`);
  
  // 监控事件循环阻塞
  const eventLoopLags: number[] = [];
  const loopMonitor = setInterval(() => {
    const t0 = performance.now();
    setImmediate(() => {
      const lag = performance.now() - t0;
      eventLoopLags.push(lag);
    });
  }, 10);

  // 1. 批量注册用户
  const users = await Promise.all(
    Array.from({ length: PAIRS * 2 }).map((_, i) => createUserAndGetToken(i))
  );

  // 2. 建立 WebSocket 连接
  const clients = users.map(u => new TestClient(u.id, u.token));
  await Promise.all(clients.map(c => c.ready));
  console.log(`✅ 成功建立 ${clients.length} 个 WebSocket 活跃连接。开始并发发送...`);

  // 3. 并发打入消息
  const tStart = Date.now();
  for (let m = 0; m < MESSAGES_PER_PAIR; m++) {
    for (let p = 0; p < PAIRS; p++) {
      const c1 = clients[p * 2];
      const c2 = clients[p * 2 + 1];
      
      // 一定概率发送大图 Base64 (比如 20% 的包)
      const isImage = Math.random() < 0.2;
      
      // 双向对发
      c1.send(c2.userId, isImage);
      c2.send(c1.userId, isImage);
    }
    // 控制一点发信速率防止瞬间 TCP 缓冲区爆炸，模拟真实人类频率
    await new Promise(r => setTimeout(r, 10)); 
  }

  // 4. 等待回显和接收完整
  console.log('⏳ 消息发送完毕，等待服务器队列处理接收...');
  await new Promise(r => setTimeout(r, 3000)); // 等待 3 秒收集余波
  
  clearInterval(loopMonitor);

  // 5. 结果统计
  let totalLatencies: number[] = [];
  clients.forEach(c => {
    totalLatencies = totalLatencies.concat(c.latencies);
    c.close();
  });

  totalLatencies.sort((a, b) => a - b);
  const p50 = totalLatencies[Math.floor(totalLatencies.length * 0.5)] || 0;
  const p95 = totalLatencies[Math.floor(totalLatencies.length * 0.95)] || 0;
  const p99 = totalLatencies[Math.floor(totalLatencies.length * 0.99)] || 0;
  
  eventLoopLags.sort((a, b) => a - b);
  const maxLag = eventLoopLags[eventLoopLags.length - 1] || 0;
  const p95Lag = eventLoopLags[Math.floor(eventLoopLags.length * 0.95)] || 0;

  console.log('\n========= 压测结果 =========');
  console.log(`耗时: ${Date.now() - tStart} ms`);
  console.log(`收发 RTT 延迟 (ms): p50=${p50}, p95=${p95}, p99=${p99}`);
  console.log(`Node.js 事件循环延迟 (ms): Max=${maxLag.toFixed(2)}, p95=${p95Lag.toFixed(2)}`);
  
  if (maxLag > 50) {
     console.warn('⚠️ 警告：事件循环严重阻塞！这说明 AES 强同步加密对高并发大文件(Base64)是巨大负担。');
  } else {
     console.log('✅ 系统承载稳定，事件循环健康。');
  }
}

main().catch(console.error);
