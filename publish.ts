import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { moderateContent } from '../services/moderation.js';

const router = Router();

const UNLIMITED_PHONES = ['19926387658', '13828577665'];
const MONTHLY_LIMIT: Record<string, number> = { free: 10, vip: 20, premium: 50 };

/** 发布信息 (会员限额 + 内容审核) */
router.post(
  '/',
  requireAuth,
  asyncHandler(async (req, res) => {
    const body = z
      .object({
        category: z.string(),
        title: z.string().min(1).max(60),
        description: z.string().min(1).max(2000),
        price: z.number().nonnegative().optional(),
        images: z.array(z.string()).max(9).default([]),
        latitude: z.number().optional(),
        longitude: z.number().optional(),
        locationName: z.string().optional(),
        attributes: z.record(z.any()).optional().default({}),
        linkedCommerceId: z.string().optional(),
        topic: z.string().optional(),
      })
      .parse(req.body);

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');

    // 本月已发数量校验 (服务端强制, 防绕过)
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const count = await prisma.post.count({ where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } } });
    const isUnlimited = user.phone && UNLIMITED_PHONES.includes(user.phone);
    const limit = isUnlimited ? 999999 : (MONTHLY_LIMIT[user.membershipTier] ?? 10);
    if (!isUnlimited && count >= limit) {
      throw new ApiError(403, `本月发布额度已用尽 (${count}/${limit}), 升级会员可提升额度`);
    }

    const moderation = await moderateContent(body.title, body.description);
    if (!moderation.pass) {
      throw new ApiError(400, `内容审核未通过: ${moderation.note}`);
    }

    const post = await prisma.post.create({
      data: {
        userId: user.id,
        category: body.category,
        title: body.title,
        description: body.description,
        price: body.price,
        images: JSON.stringify(body.images),
        latitude: body.latitude,
        longitude: body.longitude,
        locationName: body.locationName,
        linkedCommerceId: body.linkedCommerceId,
        topic: body.topic,
        attributes: JSON.stringify(body.attributes),
        status: moderation.status,
        reviewNote: moderation.note,
      },
    });

    return ok(res, { ...post, images: JSON.parse(post.images) }, moderation.status === 'published' ? '发布成功' : '已提交审核');
  }),
);

/** 发布信息流 */
router.get(
  '/',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { category, mine } = z.object({ category: z.string().optional(), mine: z.coerce.boolean().optional() }).parse(req.query);
    const where: Record<string, unknown> = { status: 'published' };
    if (category && category !== 'all') where.category = category;
    if (mine && req.userId) {
      where.userId = req.userId;
      delete where.status;
    }
    const posts = await prisma.post.findMany({ where, orderBy: { createdAt: 'desc' }, take: 50, include: { user: { select: { nickname: true, avatar: true } } } });
    return ok(res, posts.map((p) => ({ ...p, images: JSON.parse(p.images) })));
  }),
);

/** 本月发布额度 */
router.get(
  '/quota',
  requireAuth,
  asyncHandler(async (req, res) => {
    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const used = await prisma.post.count({ where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } } });
    const isUnlimited = user.phone && UNLIMITED_PHONES.includes(user.phone);
    const limit = isUnlimited ? 999999 : (MONTHLY_LIMIT[user.membershipTier] ?? 10);
    return ok(res, { used, limit, tier: user.membershipTier, remaining: isUnlimited ? 999999 : Math.max(0, limit - used) });
  }),
);

export default router;
