import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { Prisma } from '@prisma/client';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { moderateContent } from '../services/moderation.js';

const router = Router();

function mapPostUser(user: any) {
  if (!user) return null;
  const isMerchant = !!user.merchantCertification && user.merchantCertification.status === 'APPROVED';
  let authLabel = '普通用户';
  if (isMerchant) authLabel = '认证商家';
  else if (user.realNameStatus === 'verified') authLabel = '认证个人用户';
  return {
    id: user.id,
    nickname: user.nickname,
    avatar: user.avatar,
    phone: user.phone,
    isMerchant,
    authLabel,
    merchantId: isMerchant && user.ownedMerchants?.length > 0 ? user.ownedMerchants[0].id : null
  };
}


const UNLIMITED_PHONES = ['19926387658', '13828577665'];
const MONTHLY_LIMIT: Record<string, number> = { free: 10, vip: 20, premium: 50 };

const IDLE_CATEGORY_IDS = [
  'cat_idle', 'second_hand', 'cat_3c', 'cat_3c_pc', 'cat_3c_camera', 'cat_3c_audio',
  'cat_clothing', 'cat_dress', 'cat_shoes', 'cat_bag', 'cat_luxury',
  'cat_home_goods', 'cat_home_appliance', 'cat_home_furniture', 'cat_home_daily',
  'cat_beauty', 'cat_beauty_skin', 'cat_beauty_care',
  'cat_baby', 'cat_baby_clothes', 'cat_baby_stroller', 'cat_baby_toy', 'cat_baby_care',
  'cat_sports', 'cat_sports_bike', 'cat_sports_gym', 'cat_sports_camp',
  'cat_hobby', 'cat_hobby_figure', 'cat_hobby_book', 'cat_hobby_music', 'cat_hobby_pet', 'cat_hobby_ticket',
  'cat_ticket', 'cat_ticket_shop', 'cat_ticket_movie',
  'cat_other', 'cat_other_idle'
];

const ALLOWED_CATEGORIES = [
  ...IDLE_CATEGORY_IDS,
  'cat_idle_clothing',
  'cat_house',
  'house',
  'secondhand_house',
  'shop_rent',
  'cat_service',
  'housekeeping',
  'moving',
  'cat_maintenance',
  'maintenance',
  'cat_veggies',
  'veggies',
  'cat_job',
  'job',
  'cat_car_rental',
  'car_rental',
  'cat_part_time',
  'part_time',
  'cat_phone',
  'cat_laptop',
  'cat_novel',
  'cat_fruit',
] as const;

function deriveTitle(title: string | null | undefined, description: string): string {
  const t = title?.trim();
  if (t) return t.slice(0, 60);
  const line = description
    .split(/\r?\n/)
    .map((s) => s.trim())
    .find(Boolean);
  if (!line) return '闲置好物';
  return line.slice(0, 60);
}

/** 发布信息（会员限额 + 内容审核 + 真实入库） */
router.post(
  '/',
  requireAuth,
  asyncHandler(async (req, res) => {
    const body = z
      .object({
        category: z.string().min(1).max(50),
        title: z.string().max(60).nullable().optional(),
        description: z.string().min(1).max(2000),
        price: z.number().nonnegative().optional().nullable(),
        images: z.array(z.string().min(8).max(500)).max(9).default([]),
        latitude: z.number().nullable().optional(),
        longitude: z.number().nullable().optional(),
        locationName: z.string().max(80).nullable().optional(),
        publisherType: z.enum(['INDIVIDUAL', 'MERCHANT']).default('INDIVIDUAL'),
        merchantId: z.string().nullable().optional(),
        listingType: z.enum(['GOODS', 'SERVICE']).default('GOODS'),
        attributes: z.record(z.string(), z.string()).optional().default({}),
      })
      .parse(req.body);

    // 闲置类建议至少 1 张图（贴近闲鱼）
    if (IDLE_CATEGORY_IDS.includes(body.category) && body.images.length === 0) {
      throw new ApiError(400, '个人闲置请至少上传1张图片');
    }

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');

    if (body.publisherType === 'MERCHANT' && body.merchantId) {
      // 验证商家归属
      // 注意：这里假设系统通过管理员分配merchant给user，或者需要额外的关联表
      // 目前没有直接的 user-merchant 表，暂定不阻断，或者未来加入 merchant.ownerId
      // 这里可以先验证 merchant 是否存在
      const merchant = await prisma.merchant.findUnique({ where: { id: body.merchantId } });
      if (!merchant) throw new ApiError(404, '指定的商家不存在');
    }

    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const count = await prisma.post.count({
      where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } },
    });
    const isUnlimited = user.phone && UNLIMITED_PHONES.includes(user.phone);
    const limit = isUnlimited ? 999999 : (MONTHLY_LIMIT[user.membershipTier] ?? 10);
    if (!isUnlimited && count >= limit) {
      throw new ApiError(403, `本月发布额度已用尽 (${count}/${limit}), 升级会员可提升额度`);
    }

    const title = deriveTitle(body.title, body.description);
    const moderation = moderateContent(title, body.description);
    if (!moderation.pass && moderation.status === 'rejected') {
      throw new ApiError(400, `内容审核未通过: ${moderation.note}`);
    }

    const post = await prisma.post.create({
      data: {
        userId: user.id,
        publisherType: body.publisherType,
        merchantId: body.publisherType === 'MERCHANT' ? body.merchantId : null,
        listingType: body.listingType,
        category: body.category,
        title,
        description: body.description.trim(),
        price: body.price ?? null,
        images: JSON.stringify(body.images),
        latitude: body.latitude,
        longitude: body.longitude,
        locationName: body.locationName ?? '连山壮族瑶族自治县',
        attributes: JSON.stringify(body.attributes),
        status: moderation.status,
        reviewNote: moderation.note,
      },
    });

    const message =
      moderation.status === 'published'
        ? '发布成功'
        : moderation.status === 'pending_review'
          ? '已提交，等待审核'
          : '已处理';

    return ok(
      res,
      {
        ...post,
        images: JSON.parse(post.images) as string[],
        attributes: JSON.parse(post.attributes) as Record<string, string>,
      },
      message,
    );
  }),
);

/** 信息流 / 我的发布 */
router.get(
  '/',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { category, publisherId, publisherType, listingType, mine, page, pageSize, q, minPrice, maxPrice, sortBy, attrFilter } = z
      .object({
        category: z.string().optional(),
        publisherId: z.string().optional(),
        publisherType: z.enum(['INDIVIDUAL', 'MERCHANT']).optional(),
        listingType: z.enum(['GOODS', 'SERVICE']).optional(),
        mine: z.string().optional().transform(v => v === 'true'),
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
        q: z.string().optional(),
        minPrice: z.coerce.number().nonnegative().optional(),
        maxPrice: z.coerce.number().nonnegative().optional(),
        sortBy: z.enum(['latest', 'price_asc', 'price_desc']).default('latest'),
        attrFilter: z.string().optional(),
      })
      .parse(req.query);

    const where: Record<string, unknown> = { status: 'published' };
    if (category && category !== 'all') {
      const catObj = await prisma.category.findUnique({
        where: { id: category },
        include: { children: true },
      });
      if (catObj && catObj.children.length > 0) {
        const targetIds = [category, ...catObj.children.map((c) => c.id)];
        for (const child of catObj.children) {
          const grandChildren = await prisma.category.findMany({ where: { parentId: child.id } });
          targetIds.push(...grandChildren.map((gc) => gc.id));
        }
        where.category = { in: Array.from(new Set(targetIds)) };
      } else {
        const parentChildMap: Record<string, string[]> = {
          cat_idle: IDLE_CATEGORY_IDS,
          cat_house: ['cat_house', 'house', 'secondhand_house', 'shop_rent'],
          cat_service: ['cat_service', 'housekeeping', 'moving'],
          cat_maintenance: ['cat_maintenance', 'maintenance'],
          cat_veggies: ['cat_veggies', 'veggies', 'cat_fruit'],
          cat_job: ['cat_job', 'job'],
          cat_car_rental: ['cat_car_rental', 'car_rental'],
          cat_part_time: ['cat_part_time', 'part_time'],
        };
        if (parentChildMap[category]) {
          where.category = { in: parentChildMap[category] };
        } else {
          where.category = category;
        }
      }
    }
    if (publisherType) where.publisherType = publisherType;
    if (listingType) where.listingType = listingType;
    if (mine) {
      if (!req.userId) throw new ApiError(401, '未登录');
      where.userId = req.userId;
      delete where.status; // 我的发布看全部状态
    }

    if (q) {
      where.OR = [
        { title: { contains: q } },
        { description: { contains: q } }
      ];
    }

    if (minPrice !== undefined || maxPrice !== undefined) {
      where.price = {
        ...(minPrice !== undefined && { gte: minPrice }),
        ...(maxPrice !== undefined && { lte: maxPrice }),
      };
    }

    if (attrFilter) {
      try {
        const parsed = JSON.parse(attrFilter) as Record<string, string>;
        const isPg = process.env.DATABASE_URL?.startsWith('postgres');
        
        if (isPg) {
          const conditions = [];
          for (const [k, v] of Object.entries(parsed)) {
            if (v && v.trim() !== '') {
              const values = v.split('||').map(s => s.trim()).filter(Boolean);
              if (values.length > 0) {
                conditions.push(Prisma.sql`(attributes::jsonb ->> ${k} IN (${Prisma.join(values)}))`);
              }
            }
          }
          if (conditions.length > 0) {
            const rawResult = await prisma.$queryRaw<{id: string}[]>`SELECT id FROM "Post" WHERE ${Prisma.join(conditions, ' AND ')}`;
            where.id = { in: rawResult.map(r => r.id) };
          }
        } else {
          const andConditions: Record<string, unknown>[] = [];
          for (const [k, v] of Object.entries(parsed)) {
            if (v && v.trim() !== '') {
              const values = v.split('||').map(s => s.trim()).filter(Boolean);
              if (values.length > 0) {
                const valueOrs = values.flatMap(val => [
                  { attributes: { contains: `"${k}":"${val}"` } },
                  { attributes: { contains: `"${k}": "${val}"` } }
                ]);
                andConditions.push({ OR: valueOrs });
              }
            }
          }
          if (andConditions.length > 0) {
            if (Array.isArray(where.AND)) {
              (where.AND as unknown[]).push(...andConditions);
            } else if (where.AND) {
              where.AND = [where.AND, ...andConditions];
            } else {
              where.AND = andConditions;
            }
          }
        }
      } catch (e) {
        // 忽略 JSON 解析错误
      }
    }

    let orderBy: any = { createdAt: 'desc' };
    if (sortBy === 'price_asc') {
      orderBy = { price: 'asc' };
    } else if (sortBy === 'price_desc') {
      orderBy = { price: 'desc' };
    }

    const [total, posts] = await Promise.all([
      prisma.post.count({ where }),
      prisma.post.findMany({
        where,
        orderBy,
        skip: (page - 1) * pageSize,
        take: pageSize,
        include: {
          user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } }, ownedMerchants: { select: { id: true }, take: 1 } } },
          merchant: { select: { name: true, logo: true, status: true } },
        },
      }),
    ]);

    // 计算高并发下钻过滤聚合结果 (Facet Aggregations)
    // 聚合当前过滤条件下的各属性数量分布，帮助客户端进行精准过滤引导
    let aggregations: Record<string, Record<string, number>> = {};
    if (page === 1) {
      const isPostgres = process.env.DATABASE_URL?.startsWith('postgres');
      if (isPostgres) {
        try {
          // 1. 先取出所有匹配的 IDs (防注入且复用 Prisma 的组合 Where 过滤逻辑)
          const allMatchingIds = await prisma.post.findMany({
            where,
            select: { id: true }
          });

          if (allMatchingIds.length > 0) {
            const ids = allMatchingIds.map(p => p.id);
            // 2. 利用 PostgreSQL 专属的 jsonb_each_text 把 JSON keys/values 炸开，进行数据库层的 Group By
            const rawAgg = await prisma.$queryRaw<Array<{ key: string; value: string; cnt: number | bigint }>>`
              SELECT key, value, count(*) as cnt 
              FROM "Post" p, jsonb_each_text(p.attributes::jsonb) 
              WHERE p.id IN (${Prisma.join(ids)}) 
              GROUP BY key, value
            `;
            
            for (const row of rawAgg) {
              if (!row.value || row.value.trim() === '') continue;
              if (!aggregations[row.key]) aggregations[row.key] = {};
              aggregations[row.key][row.value] = Number(row.cnt);
            }
          }
        } catch (e) {
          console.error('PostgreSQL jsonb aggregation failed:', e);
        }
      }
      
      // SQLite 开发环境内存兜底逻辑 (或当 Postgres 查询失败时)
      if (!isPostgres || Object.keys(aggregations).length === 0) {
        const allMatchingPosts = await prisma.post.findMany({
          where,
          select: { attributes: true }
        });
        
        for (const p of allMatchingPosts) {
          try {
            const attrs = JSON.parse(p.attributes) as Record<string, string>;
            for (const [k, v] of Object.entries(attrs)) {
              if (!v || v.trim() === '') continue;
              if (!aggregations[k]) aggregations[k] = {};
              aggregations[k][v] = (aggregations[k][v] || 0) + 1;
            }
          } catch (e) {}
        }
      }
    }

    return ok(res, {
      total,
      page,
      pageSize,
      aggregations,
      list: posts.map((p: any) => ({
        ...p,
        user: mapPostUser(p.user),
        images: JSON.parse(p.images) as string[],
        attributes: JSON.parse(p.attributes) as Record<string, string>,
      })),
    });
  }),
);

router.get(
  '/discover',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { categoryId } = z.object({ categoryId: z.string().optional() }).parse(req.query);
    
    // 如果没有传入 categoryId 或者传了 all，我们可以找所有的 一级分类
    // 这里根据需求，通常前端会传选中的 level 1 id，比如 second_hand, house 等
    
    // 获取下一级的子分类（Level 2）
    let level2Categories: any[] = [];
    if (categoryId && categoryId !== 'all') {
      level2Categories = await prisma.category.findMany({
        where: { parentId: categoryId, isActive: true },
        orderBy: { sortOrder: 'asc' },
      });
      // fallback：有些 category 没有 isActive 字段
      if (level2Categories.length === 0) {
        level2Categories = await prisma.category.findMany({
          where: { parentId: categoryId },
          orderBy: { sortOrder: 'asc' },
        });
      }
    } else {
      // 找不到或为all时，作为演示获取前5个二级分类
      level2Categories = await prisma.category.findMany({
        where: { parentId: { not: null } },
        take: 5,
        orderBy: { sortOrder: 'asc' }
      });
    }

    const sections = [];
    for (const child of level2Categories) {
      // 找出当前 child 下的所有孙分类，包含 child 自己
      const grandChildren = await prisma.category.findMany({ where: { parentId: child.id } });
      const targetIds = [child.id, ...grandChildren.map((gc: any) => gc.id)];
      
      const posts = await prisma.post.findMany({
        where: {
          category: { in: targetIds },
          status: 'published'
        },
        orderBy: { createdAt: 'desc' },
        take: 10,
        include: {
          user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } }, ownedMerchants: { select: { id: true }, take: 1 } } },
          merchant: { select: { name: true, logo: true, status: true } },
        },
      });

      if (posts.length > 0) {
        sections.push({
          categoryId: child.id,
          categoryName: child.name,
          posts: posts.map((p: any) => ({
            ...p,
            images: JSON.parse(p.images) as string[],
            attributes: JSON.parse(p.attributes) as Record<string, string>,
          }))
        });
      } else {
        // 即便没有数据，如果是空分类也需要展示吗？
        // Joybuy 设计中没数据的可以隐藏或者展示空，这里为了展示区块，保留空区块也行
        sections.push({
          categoryId: child.id,
          categoryName: child.name,
          posts: []
        });
      }
    }

    return ok(res, sections);
  })
);

router.get(
  '/quota',
  requireAuth,
  asyncHandler(async (req, res) => {
    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const used = await prisma.post.count({
      where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } },
    });
    const isUnlimited = user.phone && UNLIMITED_PHONES.includes(user.phone);
    const limit = isUnlimited ? 999999 : (MONTHLY_LIMIT[user.membershipTier] ?? 10);
    return ok(res, { used, limit, tier: user.membershipTier, remaining: isUnlimited ? 999999 : Math.max(0, limit - used) });
  }),
);

router.get(
  '/:id',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const post = await prisma.post.findUnique({
      where: { id: req.params.id },
      include: {
        user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } }, ownedMerchants: { select: { id: true }, take: 1 } } },
        merchant: { select: { name: true, logo: true, phone: true } },
      },
    });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.status !== 'published' && post.userId !== req.userId) {
      throw new ApiError(404, '帖子不存在');
    }
    return ok(res, {
      ...post,
      images: JSON.parse(post.images) as string[],
      attributes: JSON.parse(post.attributes) as Record<string, string>,
    });
  }),
);

router.put(
  '/:id',
  requireAuth,
  asyncHandler(async (req, res) => {
    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权修改此帖子');

    const body = z
      .object({
        category: z.string().min(1).max(50),
        title: z.string().max(60).nullable().optional(),
        description: z.string().min(1).max(2000),
        price: z.number().nonnegative().optional().nullable(),
        images: z.array(z.string().min(8).max(500)).max(9).default([]),
        latitude: z.number().nullable().optional(),
        longitude: z.number().nullable().optional(),
        locationName: z.string().max(80).nullable().optional(),
        attributes: z.record(z.string(), z.string()).optional().default({}),
      })
      .parse(req.body);

    if (IDLE_CATEGORY_IDS.includes(body.category) && body.images.length === 0) {
      throw new ApiError(400, '个人闲置请至少上传1张图片');
    }

    const title = deriveTitle(body.title, body.description);
    const moderation = moderateContent(title, body.description);
    if (!moderation.pass && moderation.status === 'rejected') {
      throw new ApiError(400, `内容审核未通过: ${moderation.note}`);
    }

    const updatedPost = await prisma.post.update({
      where: { id: post.id },
      data: {
        category: body.category,
        title,
        description: body.description.trim(),
        price: body.price ?? null,
        images: JSON.stringify(body.images),
        latitude: body.latitude,
        longitude: body.longitude,
        locationName: body.locationName ?? '连山壮族瑶族自治县',
        attributes: JSON.stringify(body.attributes),
        status: moderation.status,
        reviewNote: moderation.note,
      },
    });

    return ok(
      res,
      {
        ...updatedPost,
        images: JSON.parse(updatedPost.images) as string[],
        attributes: JSON.parse(updatedPost.attributes) as Record<string, string>,
      },
      '修改成功，已重新提交审核',
    );
  }),
);

router.put(
  '/:id/status',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { status } = z.object({ status: z.enum(['removed', 'pending_review']) }).parse(req.body);
    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权操作此帖子');

    const updatedPost = await prisma.post.update({
      where: { id: post.id },
      data: { status },
    });

    return ok(
      res,
      {
        ...updatedPost,
        images: JSON.parse(updatedPost.images) as string[],
        attributes: JSON.parse(updatedPost.attributes) as Record<string, string>,
      },
      status === 'removed' ? '已成功下架' : '已重新提交审核',
    );
  }),
);

router.delete(
  '/:id',
  requireAuth,
  asyncHandler(async (req, res) => {
    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权删除此帖子');

    await prisma.post.delete({ where: { id: post.id } });
    return ok(res, null, '删除成功');
  }),
);

export default router;
