import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

const PLANS = [
  { tier: 'merchant_vip_month', name: '金牌商家包月', price: 68.0, period: '月', benefits: ['每月50条免费发布额度', 'AI智能文案无限次润色', '商家专属尊贵标识', '每月专享 15 张曝光卡'] },
  { tier: 'merchant_vip_quarter', name: '金牌商家包季', price: 168.0, period: '季', benefits: ['每月50条免费发布额度', 'AI智能文案无限次润色', '商家专属尊贵标识', '立省36元', '每月专享 20 张曝光卡'] },
  { tier: 'vip', name: '个人VIP包月', price: 19.9, period: '月', benefits: ['专属身份尊贵标识', '发布信息免审核优先', '每月专享 30 张曝光卡'] },
  { tier: 'premium', name: '至尊VIP包月', price: 39.9, period: '月', benefits: ['专属至尊尊贵标识', '发布信息免审核优先', '专属客服极速响应', '每月专享 100 张曝光卡'] },
];

/** 会员套餐 */
router.get('/plans', (_req, res) => ok(res, PLANS));

/** 订阅会员 */
router.post(
  '/subscribe',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { tier } = z.object({ tier: z.enum(['merchant_vip_month', 'merchant_vip_quarter', 'vip', 'premium']) }).parse(req.body);
    const plan = PLANS.find((p) => p.tier === tier)!;

    const endAt = new Date();
    if (tier === 'merchant_vip_quarter') {
      endAt.setMonth(endAt.getMonth() + 3);
    } else {
      endAt.setMonth(endAt.getMonth() + 1);
    }

    await prisma.subscription.create({ data: { userId: req.userId!, tier, amount: plan.price, endAt } });
    
    // 如果是个人 VIP，存入 membershipTier 为 'vip' 或 'premium'
    // 如果是商家 VIP，存入 membershipTier 为 'merchant_vip_month' 等等
    const userTier = tier;
    
    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: { membershipTier: userTier, membershipUntil: endAt },
    });

    // 赠送曝光卡 (vipCards)
    let cardsToGive = 0;
    if (tier === 'merchant_vip_month') cardsToGive = 15;
    else if (tier === 'merchant_vip_quarter') cardsToGive = 20;
    else if (tier === 'vip') cardsToGive = 30;
    else if (tier === 'premium') cardsToGive = 100;

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
