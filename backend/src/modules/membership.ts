import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

const PLANS = [
  { tier: 'merchant_vip_month', name: '金牌商家包月', price: 68.0, period: '月', benefits: ['每月50条免费发布额度', 'AI智能文案无限次润色', '商家专属尊贵标识', '每月专享 15 张曝光卡（价值 75 元）'] },
  { tier: 'merchant_vip_quarter', name: '金牌商家包季', price: 168.0, period: '季', benefits: ['每月50条免费发布额度', 'AI智能文案无限次润色', '商家专属尊贵标识', '立省36元', '每月专享 20 张曝光卡'] },
];

/** 会员套餐 */
router.get('/plans', (_req, res) => ok(res, PLANS));

/** 订阅会员 (真实场景应先支付成功再开通; 此处演示直接开通并落库) */
router.post(
  '/subscribe',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { tier } = z.object({ tier: z.enum(['merchant_vip_month', 'merchant_vip_quarter']) }).parse(req.body);
    const plan = PLANS.find((p) => p.tier === tier)!;

    const endAt = new Date();
    if (tier === 'merchant_vip_month') {
      endAt.setMonth(endAt.getMonth() + 1);
    } else {
      endAt.setMonth(endAt.getMonth() + 3);
    }

    await prisma.subscription.create({ data: { userId: req.userId!, tier, amount: plan.price, endAt } });
    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: { membershipTier: tier, membershipUntil: endAt },
    });

    // 赠送曝光卡 (vipCards)
    const cardsToGive = tier === 'merchant_vip_month' ? 15 : 20;

    const wallet = await prisma.merchantWallet.findUnique({ where: { merchantId: req.userId! } });
    const vipBefore = wallet?.vipCards || 0;

    await prisma.merchantWallet.upsert({
      where: { merchantId: req.userId! },
      update: { vipCards: { increment: cardsToGive } },
      create: {
        merchantId: req.userId!,
        vipCards: cardsToGive,
        retailCards: 0
      }
    });

    await prisma.cardTransaction.create({
      data: {
        userId: req.userId!,
        type: 'ADD',
        amount: cardsToGive,
        retailBefore: wallet?.retailCards || 0,
        retailAfter: wallet?.retailCards || 0,
        vipBefore: vipBefore,
        vipAfter: vipBefore + cardsToGive,
        reason: `开通会员赠送 (${plan.name})`
      }
    });

    return ok(res, { membershipTier: user.membershipTier, membershipUntil: user.membershipUntil }, '会员开通成功');
  })
);

export default router;
