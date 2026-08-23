import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { UserWalletService } from '../services/UserWalletService.js';

const router = Router();
const walletService = new UserWalletService();

/** 获取钱包基本信息 */
router.get(
  '/info',
  requireAuth,
  asyncHandler(async (req, res) => {
    let wallet = await prisma.userWallet.findUnique({
      where: { userId: req.userId! }
    });

    if (!wallet) {
      // 若用户暂无钱包记录，则初始化
      wallet = await prisma.userWallet.create({
        data: {
          userId: req.userId!,
          coinBalance: 0,
          totalRecharged: 0.00,
          version: 0
        }
      });
    }

    return ok(res, {
      coinBalance: wallet.coinBalance,
      totalRecharged: wallet.totalRecharged
    });
  })
);

/** 获取账单明细列表 (分页) */
router.get(
  '/logs',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { page = '1', limit = '20' } = req.query;
    const pageNum = parseInt(page as string) || 1;
    const limitNum = parseInt(limit as string) || 20;

    const total = await prisma.walletLog.count({
      where: { userId: req.userId! }
    });

    const logs = await prisma.walletLog.findMany({
      where: { userId: req.userId! },
      orderBy: { createdAt: 'desc' },
      skip: (pageNum - 1) * limitNum,
      take: limitNum
    });

    return ok(res, {
      items: logs,
      pagination: {
        page: pageNum,
        limit: limitNum,
        total,
        totalPages: Math.ceil(total / limitNum)
      }
    });
  })
);

/** 获取充值套餐列表 */
router.get(
  '/packages',
  asyncHandler(async (req, res) => {
    const packages = await prisma.rechargePackage.findMany({
      where: { isActive: true },
      orderBy: { price: 'asc' }
    });
    return ok(res, packages);
  })
);

/** 发起充值 (模拟) */
const rechargeSchema = z.object({
  packageId: z.number(),
  payChannel: z.string().optional()
});

router.post(
  '/recharge',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { packageId } = rechargeSchema.parse(req.body);

    const pkg = await prisma.rechargePackage.findUnique({
      where: { id: packageId }
    });
    if (!pkg) throw new ApiError(404, '套餐不存在');

    const amount = pkg.coinsAmount + pkg.bonusCoins;

    // 内部充值逻辑：由于是模拟，我们直接修改余额并写入流水
    await prisma.$transaction(async (tx) => {
      const wallet = await tx.userWallet.findUnique({
        where: { userId: req.userId! }
      });
      if (!wallet) throw new ApiError(400, '用户钱包不存在');

      await tx.userWallet.update({
        where: { userId: req.userId! },
        data: {
          coinBalance: wallet.coinBalance + amount,
          totalRecharged: wallet.totalRecharged + pkg.price,
          version: wallet.version + 1
        }
      });

      await tx.walletLog.create({
        data: {
          userId: req.userId!,
          amount: amount,
          balanceAfter: wallet.coinBalance + amount,
          tradeType: 'RECHARGE',
          relatedBizId: pkg.id.toString()
        }
      });
    });

    return ok(res, { success: true, message: '充值成功' });
  })
);

/** 扣费消费 */
const consumeSchema = z.object({
  amount: z.number().int().positive(),
  tradeType: z.string(),
  relatedBizId: z.string().optional()
});

router.post(
  '/consume',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { amount, tradeType, relatedBizId } = consumeSchema.parse(req.body);

    try {
      const result = await walletService.consumeCoins(req.userId!, amount, tradeType, relatedBizId);
      return ok(res, result);
    } catch (e: any) {
      if (e.message === 'INSUFFICIENT_BALANCE') {
        throw new ApiError(402, '余额不足，请充值');
      } else if (e.message === 'WALLET_NOT_FOUND') {
        throw new ApiError(404, '钱包不存在');
      } else if (e.message === 'CONCURRENT_CONFLICT') {
        throw new ApiError(409, '请求冲突，请重试');
      }
      throw e;
    }
  })
);

export default router;
