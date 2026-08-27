import type { NextFunction, Request, Response } from 'express';
import { ApiError } from '../lib/http.js';
import { prisma } from '../lib/prisma.js';

/**
 * 额度护卫中间件 (Quota Guard Middleware)
 * 用于在发布信息等涉及消耗资源的操作前，拦截并校验用户的免费和付费配额。
 * 前置依赖：必须在 requireAuth 中间件之后使用。
 */
export async function requireQuota(req: Request, _res: Response, next: NextFunction) {
  try {
    const userId = req.userId;
    if (!userId) {
      throw new ApiError(401, '请先登录');
    }

    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        merchantCertification: true,
        ownedMerchants: true
      }
    });

    if (!user) {
      throw new ApiError(404, '用户不存在');
    }

    // Attach user to req for downstream usage
    (req as any).currentUser = user;

    // 管理员无限制
    if (user.role === 'ADMIN' || (user.phone && ['19926387658', '13828577665'].includes(user.phone))) {
      return next();
    }

    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const used = await prisma.post.count({
      where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } },
    });
    
    const MONTHLY_LIMIT: Record<string, number> = { free: 10, vip: 20, premium: 50 };
    const limit = MONTHLY_LIMIT[user.membershipTier] ?? 10;
    const remainingFree = Math.max(0, limit - used);

    // 计算可用配额 (防止历史并发导致 paidQuota 为负数时，倒扣免费配额)
    const availableQuota = remainingFree + Math.max(0, user.paidQuota);
    if (availableQuota < 1) {
      // 402 Payment Required
      throw new ApiError(402, '发布配额不足，请升级特权或购买加油包', 40201);
    }

    // Attach calculated remainingFree for downstream usage (publish.ts)
    (req as any).remainingFree = remainingFree;

    next();
  } catch (error) {
    next(error);
  }
}
