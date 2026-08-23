import { Router } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAdminAuth } from '../middleware/auth.js';
import { signToken } from '../lib/jwt.js';

const router = Router();

/** 管理后台登录（公开） */
router.post(
  '/login',
  asyncHandler(async (req, res) => {
    const { username, password } = z
      .object({
        username: z.string().min(1),
        password: z.string().min(1),
      })
      .parse(req.body);

    const admin = await prisma.adminUser.findUnique({ where: { username } });
    if (!admin) throw new ApiError(401, '管理账号不存在或密码错误');

    const isValid = await bcrypt.compare(password, admin.password);
    if (!isValid) throw new ApiError(401, '管理账号不存在或密码错误');

    await prisma.adminUser.update({
      where: { id: admin.id },
      data: {
        lastLogin: new Date(),
        lastIp: req.ip || 'unknown',
      },
    });

    const token = signToken({ sub: admin.id, role: admin.role, isAdmin: true });
    return ok(res, { token, user: { username: admin.username, role: admin.role } }, '登录成功');
  }),
);

/** 以下接口一律要求管理员 JWT */
router.use(requireAdminAuth);

router.get(
  '/dashboard',
  asyncHandler(async (_req, res) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const newUsers = await prisma.user.count({ where: { createdAt: { gte: today } } });
    const activeOrders = 0;

    const payments = { _sum: { amount: 0 } };

    const pendingReviews = await prisma.post.count({ where: { status: 'pending_review' } });

    // Generate trendData for the past 7 days
    const trendData = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      d.setHours(0, 0, 0, 0);
      const nextD = new Date(d);
      nextD.setDate(nextD.getDate() + 1);
      
      const dayUsers = await prisma.user.count({
        where: { createdAt: { gte: d, lt: nextD } }
      });
      
      const dayPayments = { _sum: { amount: 0 } };

      trendData.push({
        date: d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }),
        users: dayUsers,
        revenue: dayPayments._sum.amount || 0
      });
    }

    return ok(res, {
      newUsers,
      activeOrders,
      revenue: payments._sum.amount || 0,
      pendingReviews,
      trendData
    });
  }),
);

// ================== 系统设置 ==================
router.get(
  '/settings',
  asyncHandler(async (_req, res) => {
    const configs = await prisma.systemConfig.findMany();
    const settings = configs.reduce((acc, curr) => {
      acc[curr.key] = curr.value;
      return acc;
    }, {} as Record<string, string>);
    return ok(res, settings);
  })
);

router.put(
  '/settings',
  asyncHandler(async (req, res) => {
    const { key, value } = z.object({
      key: z.string(),
      value: z.string()
    }).parse(req.body);

    const config = await prisma.systemConfig.upsert({
      where: { key },
      update: { value },
      create: { key, value }
    });

    return ok(res, config, '设置已更新');
  })
);

router.get(
  '/posts',
  asyncHandler(async (req, res) => {
    const { status } = z.object({ status: z.string().optional() }).parse(req.query);
    const where = status ? { status } : {};

    const posts = await prisma.post.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { nickname: true, phone: true } },
      },
    });

    return ok(
      res,
      posts.map((p) => ({ ...p, images: JSON.parse(p.images), attributes: JSON.parse(p.attributes) })),
    );
  }),
);

router.post(
  '/posts/:id/audit',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { action, note } = z
      .object({
        action: z.enum(['approve', 'reject', 'ban']),
        note: z.string().optional(),
      })
      .parse(req.body);

    const post = await prisma.post.findUnique({ where: { id } });
    if (!post) throw new ApiError(404, '帖子不存在');

    const newStatus = action === 'approve' ? 'published' : (action === 'ban' ? 'removed' : 'rejected');
    let reviewNote = note;
    if (!reviewNote) {
      if (action === 'approve') reviewNote = '人工审核通过';
      else if (action === 'reject') reviewNote = '人工审核拒绝';
      else reviewNote = '人工违规下架';
    }

    await prisma.post.update({
      where: { id },
      data: {
        status: newStatus,
        reviewNote,
      },
    });

    return ok(res, null, `内容已${action === 'approve' ? '通过发布' : (action === 'ban' ? '下架' : '驳回')}`);
  }),
);

router.get(
  '/kyc',
  asyncHandler(async (req, res) => {
    const { status } = z.object({ status: z.string().optional().default('pending') }).parse(req.query);
    const users = await prisma.user.findMany({
      where: { realNameStatus: status },
      orderBy: { updatedAt: 'desc' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        realName: true,
        idCardHash: true,
        realNameStatus: true,
        updatedAt: true,
      },
    });

    return ok(res, users);
  }),
);

router.post(
  '/kyc/:id/audit',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { action } = z.object({ action: z.enum(['approve', 'reject']) }).parse(req.body);

    const user = await prisma.user.findUnique({ where: { id } });
    if (!user) throw new ApiError(404, '用户不存在');
    if (user.realNameStatus !== 'pending') throw new ApiError(400, '用户当前无需审核');

    if (action === 'approve') {
      await prisma.user.update({ where: { id }, data: { realNameStatus: 'verified' } });
    } else {
      await prisma.user.update({
        where: { id },
        data: { realNameStatus: 'none', realName: null, idCardHash: null },
      });
    }

    return ok(res, null, `实名认证已${action === 'approve' ? '通过' : '驳回'}`);
  }),
);

router.get(
  '/users',
  asyncHandler(async (req, res) => {
    const { search } = z.object({ search: z.string().optional() }).parse(req.query);

    let where = {};
    if (search) {
      where = {
        OR: [{ phone: { contains: search } }, { nickname: { contains: search } }],
      };
    }

    const users = await prisma.user.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        membershipTier: true,
        walletBalance: true,
        realNameStatus: true,
        createdAt: true,
      },
    });

    return ok(res, users);
  }),
);

router.put(
  '/users/:id/balance',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { amount } = z.object({ amount: z.number() }).parse(req.body);

    const user = await prisma.user.update({
      where: { id },
      data: { walletBalance: { increment: amount } },
    });

    return ok(res, user, `成功为用户充值/扣减 ${amount} 元`);
  }),
);

router.put(
  '/users/:id/membership',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { tier } = z.object({ tier: z.enum(['free', 'vip', 'premium']) }).parse(req.body);

    const user = await prisma.user.update({
      where: { id },
      data: { membershipTier: tier },
    });

    return ok(res, user, `已将用户设为 ${tier} 会员`);
  }),
);

router.put(
  '/users/:id/status',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { status } = z.object({ status: z.enum(['active', 'banned']) }).parse(req.body);

    const user = await prisma.user.update({
      where: { id },
      data: { status },
    });

    return ok(res, user, `已将用户状态设为 ${status === 'banned' ? '封禁' : '正常'}`);
  }),
);

// ================== 财务监控 ==================
router.get(
  '/finance/stats',
  asyncHandler(async (_req, res) => {
    // 总充值现金 (bizType = recharge, type = points, BUT wait, user pays cash to get points. The payment is in Payment table or WalletTransaction?)
    // In WalletTransaction, we recorded type='points', bizType='recharge', amount > 0.
    // The actual cash paid is `payment.cashAmount`. Let's just aggregate Payment table for real cash.
    const totalCashIncome = { _sum: { cashAmount: 0 } };
    const totalPointsUsed = { _sum: { pointsUsed: 0 } };

    const settlementTransactions = await prisma.walletTransaction.aggregate({
      _sum: { amount: true },
      where: { bizType: 'settlement' }
    });
    
    // We can also compute system commission = total order amount - settled amount.
    // Let's just return basic stats.
    return ok(res, {
      totalCashIncome: totalCashIncome._sum.cashAmount || 0,
      totalPointsUsed: totalPointsUsed._sum.pointsUsed || 0,
      totalSettledToMerchants: settlementTransactions._sum.amount || 0
    });
  })
);

router.get(
  '/finance/transactions',
  asyncHandler(async (req, res) => {
    const { page = '1', limit = '20', type, bizType } = req.query as Record<string, string>;
    const p = Math.max(1, parseInt(page, 10) || 1);
    const l = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));

    let where: any = {};
    if (type) where.type = type;
    if (bizType) where.bizType = bizType;

    const [total, list] = await Promise.all([
      prisma.walletTransaction.count({ where }),
      prisma.walletTransaction.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip: (p - 1) * l,
        take: l,
        include: {
          user: { select: { phone: true, nickname: true } },
          merchant: { select: { name: true } },
        }
      })
    ]);

    return ok(res, { total, page: p, pageSize: l, list });
  })
);



// ================= 类目管理 ==================
router.get(
  '/categories',
  asyncHandler(async (_req, res) => {
    const categories = await prisma.category.findMany({
      orderBy: [{ sortOrder: 'asc' }, { createdAt: 'desc' }],
    });
    return ok(res, categories);
  })
);

router.post(
  '/categories',
  asyncHandler(async (req, res) => {
    const body = z.object({
      name: z.string(),
      iconUrl: z.string().optional(),
      parentId: z.string().optional(),
      sortOrder: z.number().default(0),
      isLeaf: z.boolean().default(false),
      isActive: z.boolean().default(true),
      attributeSchema: z.string().optional().default('[]'),
    }).parse(req.body);

    // parentId empty string check
    if (body.parentId === '') {
      delete body.parentId;
    }

    const category = await prisma.category.create({
      data: body,
    });
    return ok(res, category, '类目创建成功');
  })
);

router.put(
  '/categories/:id',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const body = z.object({
      name: z.string().optional(),
      iconUrl: z.string().optional(),
      parentId: z.string().nullable().optional(),
      sortOrder: z.number().optional(),
      isLeaf: z.boolean().optional(),
      isActive: z.boolean().optional(),
      attributeSchema: z.string().optional(),
    }).parse(req.body);

    if (body.parentId === '') {
      body.parentId = null;
    }

    const category = await prisma.category.update({
      where: { id },
      data: body,
    });
    return ok(res, category, '类目修改成功');
  })
);

router.delete(
  '/categories/:id',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    await prisma.category.delete({ where: { id } });
    return ok(res, null, '类目已删除');
  })
);

// ================== 用户管理 ==================
router.get(
  '/users',
  asyncHandler(async (req, res) => {
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 20;
    const keyword = (req.query.keyword as string) || '';

    const where = keyword
      ? {
          OR: [
            { phone: { contains: keyword } },
            { nickname: { contains: keyword } },
          ],
        }
      : {};

    const [users, total] = await Promise.all([
      prisma.user.findMany({
        where,
        skip: (page - 1) * limit,
        take: limit,
        orderBy: { createdAt: 'desc' },
        select: {
          id: true,
          phone: true,
          nickname: true,
          avatar: true,
          status: true,
          createdAt: true,
        },
      }),
      prisma.user.count({ where }),
    ]);

    return ok(res, {
      items: users,
      total,
      page,
      limit,
    });
  })
);

router.post(
  '/users/:userId/status',
  asyncHandler(async (req, res) => {
    const { userId } = req.params;
    const { status } = z.object({
      status: z.enum(['active', 'muted', 'banned']),
    }).parse(req.body);

    const user = await prisma.user.update({
      where: { id: userId },
      data: { status },
    });
    return ok(res, user, `用户状态已更新为 ${status}`);
  })
);

// ================== 举报处理 ==================
router.get(
  '/reports',
  asyncHandler(async (req, res) => {
    const status = (req.query.status as string) || 'PENDING';
    
    const reports = await prisma.report.findMany({
      where: { status },
      include: {
        reporter: {
          select: { id: true, nickname: true, avatar: true, phone: true }
        }
      },
      orderBy: { createdAt: 'desc' },
    });
    
    const enrichedReports = await Promise.all(reports.map(async (report) => {
      let targetTitle = '未知';
      if (report.targetType === 'POST') {
         const post = await prisma.post.findUnique({ where: { id: report.targetId }, select: { title: true }});
         targetTitle = post?.title || '帖子已删除';
      } else if (report.targetType === 'USER') {
         const user = await prisma.user.findUnique({ where: { id: report.targetId }, select: { nickname: true }});
         targetTitle = user?.nickname || '用户已注销';
      }
      return {
         ...report,
         targetTitle
      };
    }));

    return ok(res, enrichedReports);
  })
);

router.post(
  '/reports/:reportId/resolve',
  asyncHandler(async (req, res) => {
    const { reportId } = req.params;
    const { action } = z.object({
      action: z.enum(['IGNORE', 'CONFIRM']),
    }).parse(req.body);

    const report = await prisma.report.findUnique({ where: { id: reportId }});
    if (!report) throw new ApiError(404, '举报记录不存在');

    if (action === 'CONFIRM' && report.targetType === 'POST') {
       await prisma.post.update({
         where: { id: report.targetId },
         data: { status: 'removed' }
       }).catch(() => {});
    } else if (action === 'CONFIRM' && report.targetType === 'USER') {
       await prisma.user.update({
         where: { id: report.targetId },
         data: { status: 'banned' }
       }).catch(() => {});
    }

    const updated = await prisma.report.update({
      where: { id: reportId },
      data: { status: action === 'IGNORE' ? 'INVALID' : 'RESOLVED' },
    });

    return ok(res, updated, '举报处理完成');
  })
);

export default router;
