import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { z } from 'zod';

const router = Router();

// ================= 购买推广 (置顶, 擦亮, 急售) =================
router.post(
  '/buy',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { postId, type, days } = z.object({
      postId: z.string(),
      type: z.enum(['TOP', 'BUMP', 'TAG']),
      days: z.number().int().positive().optional().default(1) // 置顶时长
    }).parse(req.body);

    const post = await prisma.post.findUnique({
      where: { id: postId },
      include: { user: true }
    });

    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权操作');

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');

    let costCards = 0;
    if (type === 'TOP') {
      costCards = 5 * days; // 置顶 5张卡/天
    } else if (type === 'BUMP') {
      costCards = 1; // 擦亮 1张卡/次
    } else if (type === 'TAG') {
      costCards = 2; // 急售标签 2张卡/次
    }

    // 检查并扣除曝光卡与 PC 余额
    const merchantWallet = await prisma.merchantWallet.findUnique({ where: { merchantId: req.userId! } });
    const userWallet = await prisma.userWallet.findUnique({ where: { userId: req.userId! } });
    
    const retail = merchantWallet?.retailCards || 0;
    const vip = merchantWallet?.vipCards || 0;
    const pcBalance = userWallet?.coinBalance || 0;
    
    // 扣除逻辑：优先抵扣 vip 卡，其次 retail 卡，最后抵扣 PC (1卡 = 5PC)
    const PC_PER_CARD = 5;
    
    let deductVip = 0;
    let deductRetail = 0;
    let deductPc = 0;
    
    if (retail + vip >= costCards) {
      deductVip = Math.min(vip, costCards);
      deductRetail = costCards - deductVip;
    } else {
      deductVip = vip;
      deductRetail = retail;
      const remainingCards = costCards - vip - retail;
      deductPc = remainingCards * PC_PER_CARD;
      
      if (pcBalance < deductPc) {
        throw new ApiError(400, '曝光卡及 PC 余额均不足，请先充值');
      }
    }

    await prisma.$transaction(async (tx) => {
      // 1. 扣除曝光卡
      if (deductVip > 0 || deductRetail > 0) {
        await tx.merchantWallet.upsert({
          where: { merchantId: req.userId! },
          update: {
            vipCards: { decrement: deductVip },
            retailCards: { decrement: deductRetail }
          },
          create: {
            merchantId: req.userId!,
            vipCards: 0,
            retailCards: 0
          }
        });

        await tx.cardTransaction.create({
          data: {
            userId: req.userId!,
            type: 'DEDUCT',
            amount: deductVip + deductRetail,
            retailBefore: retail,
            retailAfter: retail - deductRetail,
            vipBefore: vip,
            vipAfter: vip - deductVip,
            reason: `购买帖子推广 (${type})抵扣`
          }
        });
      }

      // 2. 扣除 PC
      if (deductPc > 0) {
        await tx.userWallet.upsert({
          where: { userId: req.userId! },
          update: { coinBalance: { decrement: deductPc } },
          create: { userId: req.userId!, coinBalance: 0, totalRecharged: 0 }
        });

        await tx.walletLog.create({
          data: {
            userId: req.userId!,
            amount: -deductPc,
            balanceAfter: pcBalance - deductPc,
            tradeType: 'CONSUME_PROMOTION',
            relatedBizId: postId
          }
        });
      }
    });

    // 应用推广逻辑
    if (type === 'BUMP') {
      // 直接更新帖子的 createdAt
      await prisma.post.update({
        where: { id: postId },
        data: { createdAt: new Date() }
      });
      // 记录任务流水（即时结束）
      await prisma.promotionTask.create({
        data: {
          postId,
          userId: req.userId!,
          type,
          status: 'EXPIRED',
          endTime: new Date()
        }
      });
    } else if (type === 'TAG') {
      await prisma.post.update({
        where: { id: postId },
        data: { isUrgent: true, isSponsored: true }
      });
      await prisma.promotionTask.create({
        data: {
          postId,
          userId: req.userId!,
          type,
          status: 'EXPIRED', // 标签是买断制，写死在 Post 里，这里只存流水
          endTime: new Date()
        }
      });
    } else if (type === 'TOP') {
      const endTime = new Date(Date.now() + days * 24 * 60 * 60 * 1000);
      await prisma.promotionTask.create({
        data: {
          postId,
          userId: req.userId!,
          type,
          status: 'ACTIVE',
          endTime
        }
      });
      // 在帖子表上加一个冗余标识，方便客户端渲染 💎顶
      await prisma.post.update({
        where: { id: postId },
        data: { isTop: true, topExpireAt: endTime }
      });
    }

    return ok(res, { success: true });
  })
);

// ================= 我的推广记录 =================
router.get(
  '/my',
  requireAuth,
  asyncHandler(async (req, res) => {
    const tasks = await prisma.promotionTask.findMany({
      where: { userId: req.userId! },
      include: {
        post: { select: { id: true, title: true, images: true } }
      },
      orderBy: { createdAt: 'desc' }
    });
    return ok(res, tasks);
  })
);

// ================= 数据看板 (Hook) =================
router.get(
  '/stats',
  requireAuth,
  asyncHandler(async (req, res) => {
    // 聚合用户所有的帖子的浏览量，简单起见用 footprint 数量模拟浏览量
    const totalViews = await prisma.footprint.count({
      where: { post: { userId: req.userId! } }
    });
    const totalFavorites = await prisma.favorite.count({
      where: { post: { userId: req.userId! } }
    });
    
    const wallet = await prisma.merchantWallet.findUnique({ where: { merchantId: req.userId! } });
    const bumpCards = (wallet?.retailCards || 0) + (wallet?.vipCards || 0);

    const userWallet = await prisma.userWallet.findUnique({ where: { userId: req.userId! } });
    const pcBalance = userWallet?.coinBalance || 0;

    // 随机生成同行击败率 (只是营销噱头)
    const beatRate = 15 + Math.floor(Math.random() * 60); // 15% - 75%

    return ok(res, {
      totalViews,
      contactViews: Math.floor(totalViews * 0.1), // 模拟联系次数
      totalFavorites,
      beatRate,
      bumpCards,
      pcBalance
    });
  })
);

// ================= 充值曝光卡 =================
router.post(
  '/recharge_cards',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { quantity } = z.object({
      quantity: z.number().int().positive() // 1, 10, 30
    }).parse(req.body);

    let price = 0;
    if (quantity === 1) price = 5;
    else if (quantity === 10) price = 45;
    else if (quantity === 30) price = 100;
    else throw new ApiError(400, '无效的购买数量');

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');
    
    if (user.walletBalance < price) {
      throw new ApiError(400, '账户现金余额不足，请先充值钱包');
    }

    // 扣除现金余额
    await prisma.user.update({
      where: { id: req.userId! },
      data: { walletBalance: { decrement: price } }
    });

    await prisma.walletTransaction.create({
      data: {
        userId: req.userId!,
        type: 'cash',
        amount: -price,
        balanceBefore: user.walletBalance,
        balanceAfter: user.walletBalance - price,
        bizType: 'promotion',
        description: `购买 ${quantity} 张曝光卡`
      }
    });

    // 增加曝光卡
    const wallet = await prisma.merchantWallet.findUnique({ where: { merchantId: req.userId! } });
    const retail = wallet?.retailCards || 0;
    const vip = wallet?.vipCards || 0;

    await prisma.merchantWallet.upsert({
      where: { merchantId: req.userId! },
      update: { retailCards: { increment: quantity } },
      create: {
        merchantId: req.userId!,
        vipCards: 0,
        retailCards: quantity
      }
    });

    await prisma.cardTransaction.create({
      data: {
        userId: req.userId!,
        type: 'ADD',
        amount: quantity,
        retailBefore: retail,
        retailAfter: retail + quantity,
        vipBefore: vip,
        vipAfter: vip,
        reason: '现金购买充值'
      }
    });

    return ok(res, { success: true });
  })
);

export default router;
