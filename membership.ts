import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

const PLANS = [
  { tier: 'vip', name: '连山超级会员', price: 9.9, period: '月', benefits: ['每月20条发布额度', '免配送费券x2', '专属会员标识'] },
  { tier: 'premium', name: '连山至尊会员', price: 19.9, period: '月', benefits: ['每月50条发布额度', '免配送费券x5', '尊贵皇冠标识', '优先客服'] },
];

/** 会员套餐 */
router.get('/plans', (_req, res) => ok(res, PLANS));

import { getPaymentProvider, type PayChannel } from '../services/payment.js';
import { customAlphabet } from 'nanoid';

/** 订阅会员 (真实场景先支付再开通) */
router.post(
  '/subscribe',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { tier, channel = 'wechat' } = z.object({ 
      tier: z.enum(['vip', 'premium']), 
      channel: z.enum(['wechat', 'alipay', 'wallet', 'mock']).optional() 
    }).parse(req.body);
    const plan = PLANS.find((p) => p.tier === tier)!;

    const endAt = new Date();
    endAt.setMonth(endAt.getMonth() + 1);
    
    const subscriptionNo = 'SUB' + customAlphabet('0123456789', 8)();

    const sub = await prisma.subscription.create({ 
      data: { userId: req.userId!, tier, amount: plan.price, endAt, subscriptionNo, status: 'pending' } 
    });

    const provider = getPaymentProvider();
    const result = await provider.createPayment({
      orderNo: subscriptionNo,
      amount: plan.price,
      channel: channel as PayChannel,
      description: `连山同城-${plan.name}`,
    });

    const payment = await prisma.payment.create({
      data: {
        subscriptionId: sub.id,
        provider: channel,
        amount: plan.price,
        status: 'created',
        transactionId: result.transactionId,
        prepayPayload: JSON.stringify(result.prepayPayload),
      },
    });

    return ok(res, { paymentId: payment.id, prepayPayload: result.prepayPayload, subscriptionNo }, '订阅单创建成功，请完成支付');
  }),
);

export default router;
