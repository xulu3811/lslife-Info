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
      }),
    ]);

    return ok(res, {
      total,
      page,
      pageSize,
      list: merchants.map((m) => serialize(m)),
    });
  }),
);

/** 智能推荐 (评分 Top N) */
router.get(
  '/recommended',
  asyncHandler(async (_req, res) => {
    const merchants = await prisma.merchant.findMany({ orderBy: { rating: 'desc' }, take: 3 });
    return ok(res, merchants.map((m) => serialize(m)));
  }),
);

/** 商家详情 (兼容 externalId 与 cuid) */
router.get(
  '/:id',
  asyncHandler(async (req, res) => {
    const { id } = req.params;
    const merchant = await prisma.merchant.findFirst({
      where: { OR: [{ id }, { externalId: id }] },
    });
    if (!merchant) throw new ApiError(404, '商家不存在');
    return ok(res, serialize(merchant));
  }),
);

/** 商家入驻申请 (MerchantInfo) */
router.post(
  '/apply',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { shopName, contactPhone, address, businessHours, latitude, longitude, shopLogo, shopBanner, licenseUrl } = z
      .object({
        shopName: z.string().min(2),
        contactPhone: z.string().min(11),
        address: z.string(),
        businessHours: z.string().optional(),
        latitude: z.number().optional().nullable(),
        longitude: z.number().optional().nullable(),
        shopLogo: z.string().optional(),
        shopBanner: z.string().optional(),
        licenseUrl: z.string().optional(),
      })
      .parse(req.body);

    const merchantInfo = await prisma.merchantInfo.upsert({
      where: { userId },
      update: {
        shopName,
        contactPhone,
        address,
        businessHours,
        latitude,
        longitude,
        shopLogo,
        shopBanner,
        licenseUrl,
        verifyStatus: 'PENDING',
      },
      create: {
        userId,
        shopName,
        contactPhone,
        address,
        businessHours,
        latitude,
        longitude,
        shopLogo,
        shopBanner,
        licenseUrl,
        verifyStatus: 'PENDING',
      },
    });

    return ok(res, merchantInfo, '申请已提交，请支付认证费用或等待审核');
  })
);

/** 提交商家资质认证 */
router.post(
  '/certify',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { certType, storeName, categoryId, contactName, contactPhone, businessLicenseUrl, storePhotos, isDraft } = z
      .object({
        certType: z.enum(['ENTERPRISE', 'INDIVIDUAL']).default('ENTERPRISE'),
        storeName: z.string().default(''),
        categoryId: z.string().default(''),
        contactName: z.string().default(''),
        contactPhone: z.string().default(''),
        businessLicenseUrl: z.string().optional(),
        storePhotos: z.array(z.string()).default([]),
        isDraft: z.boolean().default(false)
      })
      .parse(req.body);
      
    const newStatus = isDraft ? 'DRAFT' : 'PENDING_REVIEW';

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
        status: newStatus as any,
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
        status: newStatus as any,
      },
    });

    return ok(res, { ...cert, storePhotos: JSON.parse(cert.storePhotos) }, isDraft ? '草稿已保存' : '认证资质已提交审核');
  })
);

/** 获取当前用户的商家认证状态 */
router.get(
  '/certify/status',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
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

/** 营业执照 OCR 模拟端点 */
router.post(
  '/certify/ocr',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { imageUrl } = z.object({ imageUrl: z.string().url() }).parse(req.body);
    // 模拟 OCR 解析延迟
    await new Promise((resolve) => setTimeout(resolve, 800));
    return ok(res, {
      legalPerson: '张三',
      creditCode: '91440300MA5XXXXX8C'
    }, '营业执照识别成功');
  })
);
