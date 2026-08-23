import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { getPaymentProvider, type PayChannel } from '../services/payment.js';
import { markOrderPaid, markSubscriptionPaid } from '../services/order-fulfillment.js';

const router = Router();

/** 创建支付 (服务端统一下单) */
router.post(
  '/create',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { orderId, channel } = z
      .object({ orderId: z.string(), channel: z.enum(['wechat', 'alipay', 'wallet', 'mock']) })
      .parse(req.body);

    const order = await prisma.order.findFirst({ where: { id: orderId, userId: req.userId! } });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status !== 'pending') throw new ApiError(400, '订单已支付或已关闭');

    // 钱包支付: 校验余额
    if (channel === 'wallet') {
      const user = await prisma.user.findUnique({ where: { id: req.userId! } });
      if (!user || user.walletBalance < order.totalAmount) throw new ApiError(400, '钱包余额不足');
    }

    const provider = getPaymentProvider();
    const result = await provider.createPayment({
      orderNo: order.orderNo,
      amount: order.totalAmount,
      channel: channel as PayChannel,
      description: `连山同城-${order.merchantName}`,
    });

    const payment = await prisma.payment.upsert({
      where: { orderId: order.id },
      update: { provider: channel, amount: order.totalAmount, status: 'created', transactionId: result.transactionId, prepayPayload: JSON.stringify(result.prepayPayload) },
      create: { orderId: order.id, provider: channel, amount: order.totalAmount, status: 'created', transactionId: result.transactionId, prepayPayload: JSON.stringify(result.prepayPayload) },
    });

    // 钱包直接扣款完成支付
    if (channel === 'wallet') {
      await prisma.user.update({ where: { id: req.userId! }, data: { walletBalance: { decrement: order.totalAmount } } });
      await markOrderPaid(order.id, result.transactionId);
      return ok(res, { paid: true, orderId: order.id }, '钱包支付成功');
    }

    return ok(res, { paymentId: payment.id, prepayPayload: JSON.parse(payment.prepayPayload!) });
  }),
);

/** 演示环境: 客户端确认 mock 支付完成 */
router.post(
  '/mock-confirm',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { orderNo } = z.object({ orderNo: z.string() }).parse(req.body);
    
    if (orderNo.startsWith('SUB')) {
      const sub = await prisma.subscription.findUnique({ where: { subscriptionNo: orderNo, userId: req.userId! } });
      if (!sub) throw new ApiError(404, '订阅单不存在');
      if (sub.status !== 'pending') return ok(res, { alreadyPaid: true });
      await markSubscriptionPaid(sub.id, `MOCK${Date.now()}`);
      return ok(res, { paid: true, subscriptionNo: orderNo }, '支付成功');
    }

    const order = await prisma.order.findFirst({ where: { orderNo, userId: req.userId! } });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status !== 'pending') return ok(res, { alreadyPaid: true });
    await markOrderPaid(order.id, `MOCK${Date.now()}`);
    return ok(res, { paid: true, orderId: order.id }, '支付成功');
  }),
);

/** 第三方支付异步回调 (微信/支付宝) */
router.post(
  '/callback/:provider',
  asyncHandler(async (req, res) => {
    const provider = getPaymentProvider();
    const result = await provider.verifyCallback(req.body, req.headers as Record<string, unknown>);
    if (result.success) {
      if (result.orderNo.startsWith('SUB')) {
        const sub = await prisma.subscription.findUnique({ where: { subscriptionNo: result.orderNo } });
        if (sub && sub.status === 'pending') {
          await markSubscriptionPaid(sub.id, result.transactionId);
        }
      } else {
        const order = await prisma.order.findUnique({ where: { orderNo: result.orderNo } });
        if (order && order.status === 'pending') {
          await markOrderPaid(order.id, result.transactionId);
        }
      }
    }
    // 第三方要求返回特定成功报文, 此处简化
    return res.json({ code: 'SUCCESS', message: 'OK' });
  }),
);

export default router;
