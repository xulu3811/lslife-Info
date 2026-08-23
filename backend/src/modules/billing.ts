import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { Prisma } from '@prisma/client';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

/** 统一下单接口 */
router.post(
  '/create-order',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { type, amount, payChannel, metadata } = z.object({
      type: z.enum(['POST_QUOTA', 'MERCHANT_VERIFY', 'POST_TOP', 'POST_REFRESH', 'BUY_URGENT_TAG', 'MERCHANT_SUBSCRIPTION']),
      amount: z.number().int().positive(), // 金额：分
      payChannel: z.enum(['WECHAT', 'ALIPAY', 'MOCK_WALLET']).default('MOCK_WALLET'),
      metadata: z.record(z.string(), z.any()).optional().nullable(),
    }).parse(req.body);

    const order = await prisma.billingOrder.create({
      data: {
        userId: req.userId!,
        type,
        amount,
        payChannel,
        status: 'PENDING',
        metadata: metadata || undefined,
      }
    });

    return ok(res, order, '订单创建成功，请继续支付');
  })
);

/** 支付回调 (Mock: 只要调用就当做支付成功，处理对应逻辑) */
router.post(
  '/callback',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { orderId } = z.object({ orderId: z.string() }).parse(req.body);

    const order = await prisma.billingOrder.findUnique({ where: { id: orderId } });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status === 'PAID') throw new ApiError(400, '订单已支付');
    if (order.userId !== req.userId) throw new ApiError(403, '无权操作此订单');

    await prisma.$transaction(async (tx) => {
      // 1. 标记订单已支付
      await tx.billingOrder.update({
        where: { id: order.id },
        data: { status: 'PAID' }
      });

      // 2. 根据订单类型下发权益
      if (order.type === 'POST_QUOTA') {
        const addedQuota = Math.floor(order.amount / 199) * 5; // e.g. 199分(1.99元)=5条配额 (Mock logic)
        await tx.user.update({
          where: { id: order.userId },
          data: { paidQuota: { increment: addedQuota || 1 } }
        });
        await tx.quotaLedger.create({
          data: {
            userId: order.userId,
            changeAmount: addedQuota || 1,
            reason: 'BUY_QUOTA'
          }
        });
      } else if (order.type === 'MERCHANT_VERIFY') {
        await tx.user.update({
          where: { id: order.userId },
          data: { role: 'MERCHANT_VERIFIED' }
        });
        await tx.merchantInfo.update({
          where: { userId: order.userId },
          data: { verifyStatus: 'APPROVED' }
        });
      } else if (order.type === 'BUY_URGENT_TAG' || order.type === 'MERCHANT_SUBSCRIPTION') {
        const userRec = await tx.user.findUnique({ where: { id: order.userId } });
        if (userRec) {
          let priv = userRec.privileges as any;
          if (typeof priv === 'string') { try { priv = JSON.parse(priv); } catch(e) { priv = {}; } }
          if (!priv || typeof priv !== 'object') priv = {};
          
          if (order.type === 'BUY_URGENT_TAG') {
            priv.urgent_tags = (priv.urgent_tags || 0) + 1;
          } else if (order.type === 'MERCHANT_SUBSCRIPTION') {
            priv.merchant_subscription = 'active';
            priv.ai_uses_left = 'unlimited';
          }
          await tx.user.update({
            where: { id: order.userId },
            data: { privileges: priv }
          });
        }
      } else if (order.type === 'POST_TOP' || order.type === 'POST_REFRESH') {
        if (!order.metadata) throw new ApiError(400, '订单缺少 metadata (postId) 信息');
        const meta = typeof order.metadata === 'string' ? JSON.parse(order.metadata) : order.metadata;
        const postId = meta.postId;
        if (!postId) throw new ApiError(400, '订单 metadata 缺少 postId');

        if (order.type === 'POST_TOP') {
          const days = meta.days || 1;
          const expireAt = new Date();
          expireAt.setDate(expireAt.getDate() + days);

          await tx.post.update({
            where: { id: postId },
            data: { isTop: true, topExpireAt: expireAt }
          });
          await tx.valueAddedLog.create({
            data: {
              userId: order.userId,
              postId: postId,
              action: 'TOP',
              cost: order.amount
            }
          });
        } else if (order.type === 'POST_REFRESH') {
          await tx.post.update({
            where: { id: postId },
            data: { refreshedAt: new Date() }
          });
          await tx.valueAddedLog.create({
            data: {
              userId: order.userId,
              postId: postId,
              action: 'REFRESH',
              cost: order.amount
            }
          });
        }
      }
    });

    return ok(res, null, '支付成功，权益已下发');
  })
);

export default router;
