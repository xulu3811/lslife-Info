import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';

const router = Router();

// GET /api/home/banners
router.get(
  '/banners',
  asyncHandler(async (_req, res) => {
    let banners = await prisma.banner.findMany({
      where: { isActive: true },
      orderBy: { sortOrder: 'asc' },
    });

    if (banners.length === 0) {
      // 预置商业 Banner 展位
      banners = [
        {
          id: 'b1',
          title: '连山特惠好房 · 盛大开盘',
          imageUrl: 'https://images.unsplash.com/photo-1560518883-ce09059eeffa?auto=format&fit=crop&w=1000&q=80',
          linkUrl: null,
          sortOrder: 1,
          isActive: true,
          createdAt: new Date(),
        },
        {
          id: 'b2',
          title: '同城品牌美食 · 8折欢聚领券',
          imageUrl: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1000&q=80',
          linkUrl: null,
          sortOrder: 2,
          isActive: true,
          createdAt: new Date(),
        },
        {
          id: 'b3',
          title: '专业家政维修 · 最快30分钟上门',
          imageUrl: 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=1000&q=80',
          linkUrl: null,
          sortOrder: 3,
          isActive: true,
          createdAt: new Date(),
        },
      ];
    }

    return ok(res, banners);
  })
);

// GET /api/home/matrix - 场景化展位矩阵 (为您甄选：同城严选/品牌好店 + 限时福利/特惠抢单)
router.get(
  '/matrix',
  asyncHandler(async (_req, res) => {
    // 找出标志为 isFeatured 或 认证商家的 Posts
    const featuredPosts = await prisma.post.findMany({
      where: {
        status: 'published',
        OR: [
          { isFeatured: true },
          { publisherType: 'MERCHANT' },
        ],
      },
      take: 4,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } } } },
        merchant: { select: { name: true, logo: true } },
      },
    });

    // 找出标志为 isSpecialOffer 或带划线原价的 Posts
    const specialOfferPosts = await prisma.post.findMany({
      where: {
        status: 'published',
      },
      take: 4,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { id: true, nickname: true, avatar: true, phone: true } },
      },
    });

    const mapPost = (p: any) => ({
      ...p,
      images: JSON.parse(p.images) as string[],
      attributes: (p.attributes as any) as Record<string, string>,
    });

    return ok(res, {
      featuredMerchants: featuredPosts.map(mapPost),
      specialOffers: specialOfferPosts.map(mapPost),
    });
  })
);

export default router;
