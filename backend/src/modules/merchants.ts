import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

function serialize(m: { tags: string } & Record<string, unknown>) {
  return { ...m, tags: JSON.parse(m.tags) as string[] };
}

/** 商家列表 (支持分类/搜索/排序/分页) */
router.get(
  '/',
  asyncHandler(async (req, res) => {
    const { category, q, sort, page, pageSize } = z
      .object({
        category: z.string().optional(),
        q: z.string().optional(),
        sort: z.enum(['default', 'distance', 'sales', 'rating']).default('default'),
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
      })
      .parse(req.query);

    const where: Record<string, unknown> = { status: 'active' };
    if (category && category !== 'all') where.category = category;
    if (q) where.OR = [{ name: { contains: q } }, { description: { contains: q } }];

    const orderBy =
      sort === 'distance'
        ? { distance: 'asc' as const }
        : sort === 'sales'
          ? { sales: 'desc' as const }
          : sort === 'rating'
            ? { rating: 'desc' as const }
            : { sales: 'desc' as const };

    const [total, merchants] = await Promise.all([
      prisma.merchant.count({ where }),
      prisma.merchant.findMany({
        where,
        orderBy,
        skip: (page - 1) * pageSize,
        take: pageSize,
        include: { products: true },
      }),
    ]);

    return ok(res, {
      total,
      page,
      pageSize,
      list: merchants.map((m) => ({ ...serialize(m), items: m.products })),
    });
  }),
);

/** 智能推荐 (评分 Top N) */
router.get(
  '/recommended',
  asyncHandler(async (_req, res) => {
    const merchants = await prisma.merchant.findMany({ orderBy: { rating: 'desc' }, take: 3, include: { products: true } });
    return ok(res, merchants.map((m) => ({ ...serialize(m), items: m.products })));
  }),
);

/** 商家详情 (兼容 externalId 与 cuid) */
router.get(
  '/:id',
  asyncHandler(async (req, res) => {
    const { id } = req.params;
    const merchant = await prisma.merchant.findFirst({
      where: { OR: [{ id }, { externalId: id }] },
      include: { products: true },
    });
    if (!merchant) throw new ApiError(404, '商家不存在');
    return ok(res, { ...serialize(merchant), items: merchant.products });
  }),
);

/** 商家入驻申请 */
router.post(
  '/apply',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = (req as any).user!.id;
    const { name, phone, address, description, category, latitude, longitude, logo, banner } = z
      .object({
        name: z.string().min(2),
        phone: z.string().min(11),
        address: z.string(),
        description: z.string(),
        category: z.string().optional(),
        latitude: z.number(),
        longitude: z.number(),
        logo: z.string(),
        banner: z.string(),
      })
      .parse(req.body);

    const config = await prisma.systemConfig.findUnique({ where: { key: 'merchant_require_approval' } });
    const requireApproval = config?.value === 'true';

    const merchant = await prisma.merchant.create({
      data: {
        ownerId: userId,
        name,
        phone,
        address,
        description,
        category,
        latitude,
        longitude,
        logo,
        banner,
        status: requireApproval ? 'pending' : 'active',
      },
    });

    return ok(res, merchant, requireApproval ? '申请已提交，请等待管理员审核' : '店铺创建成功');
  })
);

/** 提交商家资质认证 */
router.post(
  '/certify',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = (req as any).user!.id;
    const { certType, storeName, categoryId, contactName, contactPhone, businessLicenseUrl, storePhotos } = z
      .object({
        certType: z.enum(['ENTERPRISE', 'INDIVIDUAL']).default('ENTERPRISE'),
        storeName: z.string().min(2),
        categoryId: z.string().default(''),
        contactName: z.string().min(2),
        contactPhone: z.string().min(11),
        businessLicenseUrl: z.string().optional(),
        storePhotos: z.array(z.string()).default([]),
      })
      .parse(req.body);

    const cert = await prisma.merchantCertification.upsert({
      where: { userId },
      update: {
        certType,
        storeName,
        categoryId,
        contactName,
        contactPhone,
        businessLicenseUrl,
        storePhotos: JSON.stringify(storePhotos),
        status: 'PENDING',
        rejectReason: null,
      },
      create: {
        userId,
        certType,
        storeName,
        categoryId,
        contactName,
        contactPhone,
        businessLicenseUrl,
        storePhotos: JSON.stringify(storePhotos),
        status: 'PENDING',
      },
    });

    return ok(res, cert, '认证资质已提交审核');
  })
);

/** 获取当前用户的商家认证状态 */
router.get(
  '/certify/status',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = (req as any).user!.id;
    const cert = await prisma.merchantCertification.findUnique({
      where: { userId },
    });

    if (!cert) {
      return ok(res, null, '未提交认证');
    }

    return ok(res, {
      ...cert,
      storePhotos: JSON.parse(cert.storePhotos),
    });
  })
);

export default router;
