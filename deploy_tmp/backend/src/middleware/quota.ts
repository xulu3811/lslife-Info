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
      select: {
        role: true,
        freeQuota: true,
        paidQuota: true,
      },
    });

    if (!user) {
      throw new ApiError(404, '用户不存在');
    }

    // Attach user to req for downstream usage
    (req as any).currentUser = user;

    // 管理员无限制
    if (user.role === 'ADMIN') {
      return next();
    }

    // 计算可用配额
    const availableQuota = user.freeQuota + user.paidQuota;
    if (availableQuota < 1) {
      // 402 Payment Required
      throw new ApiError(402, '发布配额不足，请升级特权或购买加油包', 40201);
    }

    next();
  } catch (error) {
    next(error);
  }
}
