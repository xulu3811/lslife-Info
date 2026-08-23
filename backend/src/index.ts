import http from 'node:http';
import { createApp } from './app.js';
import { attachRealtime } from './realtime/hub.js';
import { env } from './config/env.js';
import { prisma } from './lib/prisma.js';

const app = createApp();
const server = http.createServer(app);
attachRealtime(server);

// ================= 推广中心：定时过期任务 =================
setInterval(async () => {
  try {
    const expiredTasks = await prisma.promotionTask.findMany({
      where: { status: 'ACTIVE', endTime: { lt: new Date() } }
    });

    if (expiredTasks.length > 0) {
      const taskIds = expiredTasks.map(t => t.id);
      const postIds = expiredTasks.map(t => t.postId);

      // 1. 将任务设为过期
      await prisma.promotionTask.updateMany({
        where: { id: { in: taskIds } },
        data: { status: 'EXPIRED' }
      });

      // 2. 将帖子设为不再置顶
      await prisma.post.updateMany({
        where: { id: { in: postIds } },
        data: { isTop: false, topExpireAt: null }
      });
      console.log(`[Cron] 已过期 ${expiredTasks.length} 个置顶推广任务`);
    }
  } catch (error) {
    console.error('[Cron] 推广过期任务执行失败:', error);
  }
}, 60000); // 每分钟执行一次

// ================= 月底清零 VIP 曝光卡任务 (模拟) =================
// 每天凌晨自检是否是每月 1 号，如果是则执行清零
setInterval(async () => {
  const now = new Date();
  if (now.getDate() === 1 && now.getHours() === 0) {
    try {
      // 记录流水 (在重置之前)
      const wallets = await prisma.merchantWallet.findMany({ where: { vipCards: { gt: 0 } } });
      for (const w of wallets) {
        await prisma.cardTransaction.create({
          data: {
            userId: w.merchantId,
            type: 'DEDUCT',
            amount: w.vipCards,
            retailBefore: w.retailCards,
            retailAfter: w.retailCards,
            vipBefore: w.vipCards,
            vipAfter: 0,
            reason: '月初自动清零 VIP 赠送曝光卡'
          }
        });
      }

      await prisma.merchantWallet.updateMany({
        where: { vipCards: { gt: 0 } },
        data: { vipCards: 0 }
      });
      console.log(`[Cron] 已自动清零 ${wallets.length} 个用户的 VIP 曝光卡`);
    } catch (e) {
      console.error('[Cron] 清零 VIP 曝光卡失败:', e);
    }
  }
}, 1000 * 60 * 60); // 每小时执行一次检查

server.listen(env.port, () => {
  console.log(`\n连山同城 LsLife 后端已启动`);
  console.log(`  HTTP  : http://localhost:${env.port}/api`);
  console.log(`  WS    : ws://localhost:${env.port}/ws?token=<JWT>`);
  console.log(`  健康检查: http://localhost:${env.port}/api/health\n`);
});
