import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { Prisma } from '@prisma/client';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { requireQuota } from '../middleware/quota.js';
import { moderateContent } from '../services/moderation.js';
import { createHash } from 'node:crypto';

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
  requireQuota,
  asyncHandler(async (req, res) => {
    const body = z
      .object({
        category: z.string().min(1).max(50),
        title: z.string().max(60).nullable().optional(),
        description: z.string().min(1).max(2000),
        price: z.number().nonnegative().optional().nullable(),
        images: z.array(z.string().min(8).max(500)).max(9).default([]),
        province: z.string().max(40).nullable().optional(),
        city: z.string().max(40).nullable().optional(),
        district: z.string().max(40).nullable().optional(),
        town: z.string().max(60).nullable().optional(),
        streetAddress: z.string().max(200).nullable().optional(),
        publisherType: z.enum(['INDIVIDUAL', 'MERCHANT']).default('INDIVIDUAL'),
        merchantId: z.string().nullable().optional(),
        listingType: z.enum(['GOODS', 'SERVICE']).default('GOODS'),
        postType: z.enum(['CLASSIFIED', 'MOMENT']).default('CLASSIFIED'),
        tradeMode: z.string().optional().default('INFO'),
        linkedCommerceId: z.string().nullable().optional(),
        attributes: z.record(z.string(), z.any()).optional().default({}),
        topic: z.string().max(30).nullable().optional(),
        contactPhone: z.string().nullable().optional(),
        imageHashes: z.array(z.string()).optional().default([]),
        useUrgentTag: z.boolean().optional().default(false),
      })
      .superRefine((data, ctx) => {
        if (data.category === 'sys_dynamic') {
          if (data.price !== null && data.price !== undefined) {
            ctx.addIssue({ code: z.ZodIssueCode.custom, message: "同城动态不支持设定价格" });
          }
          if (data.linkedCommerceId) {
            ctx.addIssue({ code: z.ZodIssueCode.custom, message: "同城动态不能关联商品" });
          }
          if (data.images.length > 6) {
            ctx.addIssue({ code: z.ZodIssueCode.custom, message: "同城动态最多支持6张图片" });
          }
        }
      })
      .parse(req.body);

    // 闲置类建议至少 1 张图（贴近闲鱼）
    if (IDLE_CATEGORY_IDS.includes(body.category) && body.images.length === 0) {
      throw new ApiError(400, '个人闲置请至少上传1张图片');
    }

    const user = (req as any).currentUser;

    if (body.publisherType === 'MERCHANT' && body.merchantId) {
      // 验证商家归属
      const merchant = await prisma.merchant.findUnique({ where: { id: body.merchantId } });
      if (!merchant) throw new ApiError(404, '指定的商家不存在');
    }

    const isAdmin = user.role === 'ADMIN' || user.role === 'SUPERADMIN';
    const isMerchant = !!user.merchantCertification && user.merchantCertification.status === 'APPROVED';

    // 1. 设备校验: 拦截高风险设备
    if (req.headers['x-device-risk'] === 'high' && !isAdmin) {
      throw new ApiError(403, '检测到当前环境存在安全风险，仅允许浏览操作');
    }

    // 2. 阶梯式发帖配额
    let isUrgent = false;

    let privileges: any = user.privileges;
    if (typeof privileges === 'string') {
      try { privileges = JSON.parse(privileges); } catch (e) { privileges = {}; }
    }
    if (!privileges || typeof privileges !== 'object') privileges = {};

    if (!isAdmin) {
      if (user.realNameStatus !== 'verified' && !isMerchant) {
        throw new ApiError(403, '未实名认证用户仅可浏览，不可发布。请先进行实名认证。');
      }

      if (body.useUrgentTag) {
        if ((privileges.urgent_tags || 0) <= 0) {
          throw new ApiError(403, '急售标签余量不足，请前往购买');
        }
        isUrgent = true;
      }

      const activePostsCount = await prisma.post.count({ where: { userId: user.id, status: { in: ['published', 'PUBLISHED'] } } });
      const todayStart = new Date();
      todayStart.setHours(0, 0, 0, 0);
      const todayPostsCount = await prisma.post.count({ where: { userId: user.id, createdAt: { gte: todayStart } } });

      if (isMerchant) {
        const isSubscribed = privileges.merchant_subscription === 'active';
        if (!isSubscribed) {
          if (activePostsCount >= 10) throw new ApiError(403, '免费版商家在架商品已达上限 (10个)，请升级商家套餐');
          if (todayPostsCount >= 5) throw new ApiError(403, '免费版商家今日发布数量已达上限 (5条)');
        }
      } else {
        if (activePostsCount >= 3 && !isUrgent) {
          throw new ApiError(403, '在架信息已达个人免费上限 (3条)，请使用急售标签发布或下架其他信息。');
        }
        if (todayPostsCount >= 2 && !isUrgent) {
          throw new ApiError(403, '今日发布数量已达免费上限 (2条)，请使用急售标签继续发布。');
        }
      }
    }

    const title = deriveTitle(body.title, body.description);
    
    // 3. 防刷屏查重与高频词熔断
    const textHash = createHash('md5').update((title + body.description).trim()).digest('hex');
    let isShadowBanned = false;
    
    // 检查其他用户的重复内容
    if (!isAdmin) {
      const duplicate = await prisma.post.findFirst({
        where: {
          userId: { not: user.id },
          OR: [
            { textHash },
          ]
        }
      });
      if (duplicate) {
        isShadowBanned = true;
      } else if (body.imageHashes.length > 0) {
        // 简单图片哈希撞库
        for (const h of body.imageHashes) {
          const dupImg = await prisma.post.findFirst({
             where: { userId: { not: user.id }, imageHashes: { contains: `"${h}"` } }
          });
          if (dupImg) {
            isShadowBanned = true;
            break;
          }
        }
      }
    }

    const moderation = moderateContent(title, body.description);
    
    // 触发全局高频违规词熔断，转入人工
    if (/(无抵押贷款|兼职刷单)/.test(title + body.description)) {
      moderation.status = 'MANUAL_REVIEWING';
    }

    if (!moderation.pass && moderation.status === 'REJECTED') {
      throw new ApiError(400, `内容审核未通过: ${moderation.note}`);
    }

    const post = await prisma.$transaction(async (tx) => {
      const newPost = await tx.post.create({
        data: {
          userId: user.id,
          publisherType: body.publisherType,
          merchantId: body.publisherType === 'MERCHANT' ? body.merchantId : null,
          listingType: body.listingType,
          postType: body.postType,
          tradeMode: body.tradeMode,
          linkedCommerceId: body.linkedCommerceId ?? null,
          category: body.category,
          title,
          description: body.description.trim(),
          price: body.price ?? null,
          images: JSON.stringify(body.images),
          province: body.province,
          city: body.city,
          district: body.district,
          town: body.town,
          streetAddress: body.streetAddress,
          attributes: JSON.stringify(body.attributes),
          status: moderation.status,
          reviewNote: moderation.note,
          topic: body.topic ?? null,
          contactPhone: body.contactPhone ?? null,
          textHash,
          imageHashes: JSON.stringify(body.imageHashes),
          isShadowBanned,
          isUrgent,
        },
      });

      if (!isAdmin && moderation.status !== 'REJECTED') {
        if (isUrgent) {
          privileges.urgent_tags = Math.max(0, (privileges.urgent_tags || 0) - 1);
          await tx.user.update({
            where: { id: user.id },
            data: { privileges: privileges }
          });
          await tx.quotaLedger.create({
            data: { userId: user.id, changeAmount: -1, reason: `POST_CONSUME_URGENT` }
          });
        } else {
          const usedFree = user.freeQuota > 0;
          await tx.user.update({
            where: { id: user.id },
            data: usedFree ? { freeQuota: { decrement: 1 } } : { paidQuota: { decrement: 1 } }
          });

          await tx.quotaLedger.create({
            data: {
              userId: user.id,
              changeAmount: -1,
              reason: `POST_CONSUME_${usedFree ? 'FREE' : 'PAID'}`
            }
          });
        }
      }

      return newPost;
    });

    // 触发异步 AI 审核队列
    if (moderation.status === 'AI_REVIEWING') {
      import('../services/moderation.js').then(m => {
        let parsedImages: string[] = [];
        try { parsedImages = JSON.parse(post.images); } catch (e) {}
        m.triggerAiReview(post.id, post.title, post.description, parsedImages);
      }).catch(console.error);
    }

    const message =
      moderation.status === 'PUBLISHED'
        ? '发布成功'
        : moderation.status === 'MANUAL_REVIEWING'
          ? '已提交人工审核'
          : moderation.status === 'AI_REVIEWING'
            ? '已提交智能审核中...'
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
    const { category, publisherId, publisherType, listingType, mine, page, pageSize, q, minPrice, maxPrice, sortBy, attrFilter, postType } = z
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
        sortBy: z.enum(['latest', 'price_asc', 'price_desc', 'recommend']).default('latest'),
        attrFilter: z.string().optional(),
        postType: z.enum(['CLASSIFIED', 'MOMENT']).optional(),
      })
      .parse(req.query);

    const where: Record<string, unknown> = { status: { in: ['published', 'PUBLISHED'] } };
    if (!mine) {
      where.isShadowBanned = false; // 非“我的”不展示被影子封禁的帖子
    }
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
    if (postType) where.postType = postType;
    if (publisherId) where.userId = publisherId;
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
    } else if (sortBy === 'recommend') {
      // TODO: 混合推荐算法预留 (Algorithm Stub)
      // 1. 混合权重机制：综合考虑 createdAt (40%) + 基于 LBS 经纬度的距离衰减 (40%) + likeCount (20%)
      // 2. 强制插桩 (Sponsored Injection)：
      //    如果拉取到的数据中 isSponsored == true，需在返回给前端的 List 中将其强制安插在特定的索引位
      //    （例如每隔 4 位 或 10 位插入），保证广告曝光率且避免连片出现。
      // 当前暂时以点赞数结合时间倒序代替
      orderBy = [{ isSponsored: 'desc' }, { likeCount: 'desc' }, { createdAt: 'desc' }];
    }

    let [total, posts] = await Promise.all([
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

    // 强制曝光冷却与折叠逻辑 (仅对非“我的”聚合列表生效)
    if (!mine) {
      const tenMinsAgo = new Date(Date.now() - 10 * 60 * 1000);
      const userRecentPostCount: Record<string, number> = {};
      const filteredPosts = [];

      for (const p of posts) {
        if (p.createdAt > tenMinsAgo) {
          userRecentPostCount[p.userId] = (userRecentPostCount[p.userId] || 0) + 1;
          if (userRecentPostCount[p.userId] > 1) {
            continue; // 折叠
          }
        }
        filteredPosts.push(p);
      }
      posts = filteredPosts;

      // 推广中心：在第 1 页置顶 ACTIVE TOP 推广的帖子
      if (page === 1) {
        const topPromotions = await prisma.promotionTask.findMany({
          where: { 
            type: 'TOP', 
            status: 'ACTIVE',
            post: where
          },
          include: {
            post: {
              include: {
                user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } }, ownedMerchants: { select: { id: true }, take: 1 } } },
                merchant: { select: { name: true, logo: true, status: true } }
              }
            }
          },
          orderBy: { startTime: 'desc' }
        });
        
        // 获取所有要置顶的帖子，标记 isTop = true (由于通过 promotionTask 获取，不影响实际库结构，动态赋予)
        const topPosts = topPromotions.map(pt => ({ ...pt.post, isTop: true }));
        const topPostIds = new Set(topPosts.map(p => p.id));

        // 从普通 posts 中剔除已被置顶的，避免重复出现
        posts = posts.filter(p => !topPostIds.has(p.id));

        // 插入头部
        posts = [...topPosts, ...posts];
      }
    }

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
            // 2. 利用 PostgreSQL 专属的 jsonb_each_text把 JSON keys/values 炸开，进行数据库层的 Group By
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
        attributes: JSON.parse(p.attributes) as Record<string, string>
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
          status: { in: ['published', 'PUBLISHED'] },
          postType: 'CLASSIFIED' // 仅显示分类信息帖子
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
  '/favorites',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { page, pageSize } = z
      .object({
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
      })
      .parse(req.query);

    const total = await prisma.favorite.count({ where: { userId } });
    const favorites = await prisma.favorite.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      skip: (page - 1) * pageSize,
      take: pageSize,
      include: {
        post: {
          include: {
            user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } }, ownedMerchants: { select: { id: true }, take: 1 } } },
            merchant: { select: { name: true, logo: true, phone: true } },
          },
        },
      },
    });

    const posts = favorites
      .filter((f) => f.post != null)
      .map((f) => {
        const p = f.post;
        return {
          ...p,
          isFavorite: true,
          user: mapPostUser(p.user),
          images: JSON.parse(p.images || '[]') as string[],
          attributes: JSON.parse(p.attributes || '{}') as Record<string, string>,
        };
      });

    return ok(res, {
      total,
      page,
      pageSize,
      list: posts,
    });
  }),
);

router.get(
  '/footprints',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { page, pageSize } = z
      .object({
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(50),
      })
      .parse(req.query);

    const total = await prisma.footprint.count({ where: { userId } });
    const footprints = await prisma.footprint.findMany({
      where: { userId },
      orderBy: { viewedAt: 'desc' },
      skip: (page - 1) * pageSize,
      take: pageSize,
      include: {
        post: {
          include: {
            user: { select: { id: true, nickname: true, avatar: true, phone: true, realNameStatus: true, merchantCertification: { select: { status: true } }, ownedMerchants: { select: { id: true }, take: 1 } } },
            merchant: { select: { name: true, logo: true, phone: true } },
          },
        },
      },
    });

    const posts = footprints
      .filter((f) => f.post != null)
      .map((f) => {
        const p = f.post;
        return {
          ...p,
          isFavorite: false, // Could be hydrated if needed, but not strictly required for footprint list
          user: mapPostUser(p.user),
          images: JSON.parse(p.images || '[]') as string[],
          attributes: JSON.parse(p.attributes || '{}') as Record<string, string>,
        };
      });

    return ok(res, {
      total,
      page,
      pageSize,
      list: posts,
    });
  }),
);

router.delete(
  '/footprints',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    await prisma.$transaction([
      prisma.footprint.deleteMany({ where: { userId } }),
      prisma.user.update({ where: { id: userId }, data: { footprintsCount: 0 } })
    ]);
    return ok(res, null);
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

    let isAdmin = false;
    if (req.userId) {
      const u = await prisma.user.findUnique({ where: { id: req.userId }, select: { role: true } });
      isAdmin = (u?.role === 'ADMIN' || u?.role === 'SUPERADMIN');
    }

    if (post.status !== 'published' && post.status !== 'PUBLISHED' && post.userId !== req.userId && !isAdmin) {
      throw new ApiError(404, '帖子不存在');
    }
    
    let isFavorite = false;
    let isFollowing = false;
    if (req.userId) {
      const fav = await prisma.favorite.findUnique({
        where: { userId_postId: { userId: req.userId, postId: post.id } }
      });
      isFavorite = !!fav;

      const follow = await prisma.follow.findUnique({
        where: { followerId_followingId: { followerId: req.userId, followingId: post.userId } }
      });
      isFollowing = !!follow;

      // 记录足迹 (异步执行，不阻塞主流程)
      (async () => {
        try {
          const userId = req.userId!;
          await prisma.footprint.upsert({
            where: { userId_postId: { userId, postId: post.id } },
            create: { userId, postId: post.id },
            update: { viewedAt: new Date() }
          });
          
          // 限制最多 50 条
          const footprintCount = await prisma.footprint.count({ where: { userId } });
          if (footprintCount > 50) {
            const excess = await prisma.footprint.findMany({
              where: { userId },
              orderBy: { viewedAt: 'desc' },
              skip: 50,
              select: { id: true }
            });
            if (excess.length > 0) {
              await prisma.footprint.deleteMany({
                where: { id: { in: excess.map(e => e.id) } }
              });
            }
          }
          // 同步总数
          const finalCount = await prisma.footprint.count({ where: { userId } });
          await prisma.user.update({ where: { id: userId }, data: { footprintsCount: finalCount } });
        } catch (e) {
          console.error("记录足迹失败:", e);
        }
      })();
    }

    return ok(res, {
      ...post,
      isFavorite,
      isFollowing,
      images: JSON.parse(post.images) as string[],
      attributes: JSON.parse(post.attributes) as Record<string, string>,
    });
  }),
);

router.post(
  '/:id/favorite',
  requireAuth,
  asyncHandler(async (req, res) => {
    const postId = req.params.id;
    const userId = req.userId!;
    
    const post = await prisma.post.findUnique({ where: { id: postId } });
    if (!post) throw new ApiError(404, '帖子不存在');

    const existingFav = await prisma.favorite.findUnique({
      where: { userId_postId: { userId, postId } }
    });

    let newLikeCount = post.likeCount;
    let isFavorite = false;

    if (existingFav) {
      await prisma.$transaction([
        prisma.favorite.delete({ where: { id: existingFav.id } }),
        prisma.post.update({ where: { id: postId }, data: { likeCount: { decrement: 1 } } }),
        prisma.user.update({ where: { id: userId }, data: { favoritesCount: { decrement: 1 } } })
      ]);
      newLikeCount = Math.max(0, post.likeCount - 1);
    } else {
      await prisma.$transaction([
        prisma.favorite.create({ data: { userId, postId } }),
        prisma.post.update({ where: { id: postId }, data: { likeCount: { increment: 1 } } }),
        prisma.user.update({ where: { id: userId }, data: { favoritesCount: { increment: 1 } } })
      ]);
      newLikeCount = post.likeCount + 1;
      isFavorite = true;
    }

    return ok(res, { isFavorite, likeCount: newLikeCount }, isFavorite ? '收藏成功' : '已取消收藏');
  })
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
        province: z.string().max(40).nullable().optional(),
        city: z.string().max(40).nullable().optional(),
        district: z.string().max(40).nullable().optional(),
        town: z.string().max(60).nullable().optional(),
        streetAddress: z.string().max(200).nullable().optional(),
        postType: z.enum(['CLASSIFIED', 'COMMERCE']).optional(),
        tradeMode: z.string().optional(),
        linkedCommerceId: z.string().nullable().optional(),
        attributes: z.record(z.string(), z.any()).optional().default({}),
        contactPhone: z.string().nullable().optional(),
      })
      .parse(req.body);

    if (IDLE_CATEGORY_IDS.includes(body.category) && body.images.length === 0) {
      throw new ApiError(400, '个人闲置请至少上传1张图片');
    }

    const title = deriveTitle(body.title, body.description);
    const moderation = moderateContent(title, body.description);
    if (!moderation.pass && moderation.status === 'REJECTED') {
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
        province: body.province,
        city: body.city,
        district: body.district,
        town: body.town,
        streetAddress: body.streetAddress,
        tradeMode: body.tradeMode,
        linkedCommerceId: body.linkedCommerceId !== undefined ? (body.linkedCommerceId ?? null) : undefined,
        attributes: JSON.stringify(body.attributes),
        contactPhone: body.contactPhone ?? null,
        status: moderation.status,
        reviewNote: moderation.note,
      },
    });

    if (moderation.status === 'AI_REVIEWING') {
      import('../services/moderation.js').then(m => {
        m.triggerAiReview(updatedPost.id, updatedPost.title, updatedPost.description);
      }).catch(console.error);
    }

    const message =
      moderation.status === 'PUBLISHED'
        ? '修改成功'
        : moderation.status === 'MANUAL_REVIEWING'
          ? '已修改，等待人工审核'
          : moderation.status === 'AI_REVIEWING'
            ? '已修改，提交智能审核中...'
            : '已处理';

    return ok(
      res,
      {
        ...updatedPost,
        images: JSON.parse(updatedPost.images) as string[],
        attributes: JSON.parse(updatedPost.attributes) as Record<string, string>,
      },
      message,
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

/** 帖子增值服务: 创建置顶/擦亮订单 */
router.post(
  '/:id/boost',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { action, days } = z.object({
      action: z.enum(['TOP', 'REFRESH']),
      days: z.number().int().positive().optional().default(1),
    }).parse(req.body);

    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权操作此帖子');

    // 计价规则
    let amount = 0;
    let type = '';
    const metadata: any = { postId: post.id };

    if (action === 'REFRESH') {
      amount = 100; // 1元/次
      type = 'POST_REFRESH';
    } else if (action === 'TOP') {
      amount = 500 * days; // 5元/天
      type = 'POST_TOP';
      metadata.days = days;
    }

    const order = await prisma.billingOrder.create({
      data: {
        userId: req.userId!,
        type,
        amount,
        payChannel: 'WECHAT', // 默认或者支持客户端传入，目前先固定
        status: 'PENDING',
        metadata: metadata || undefined,
      }
    });

    return ok(res, order, '订单创建成功，请继续支付');
  }),
);

export default router;
