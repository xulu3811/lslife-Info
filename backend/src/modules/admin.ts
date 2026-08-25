import { Router } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { pushToUser, broadcastToAll } from '../realtime/hub.js';
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

    const pendingReviews = await prisma.post.count({ 
      where: { status: { in: ['pending_review', 'MANUAL_REVIEWING'] } } 
    });
    
    const pendingProfileReviews = await prisma.user.count({
      where: { profileReviewStatus: 'MANUAL_REVIEWING' }
    });
    
    const pendingKyc = await prisma.user.count({
      where: { realNameStatus: 'pending' }
    });
    
    const pendingMerchantCerts = await prisma.merchantCertification.count({
      where: { status: 'PENDING_REVIEW' }
    });

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
      pendingProfileReviews,
      pendingKyc,
      pendingMerchantCerts,
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
    let where: any = {};
    if (status) {
      if (status === 'pending_review' || status === 'MANUAL_REVIEWING') {
        where.status = { in: ['pending_review', 'MANUAL_REVIEWING'] };
      } else {
        where.status = status;
      }
    }

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
      pushToUser(id, { event: 'USER_STATE_CHANGED', message: '您的实名认证已通过！' });
    } else {
      await prisma.user.update({
        where: { id },
        data: { realNameStatus: 'none', realName: null, idCardHash: null },
      });
      pushToUser(id, { event: 'USER_STATE_CHANGED', message: '您的实名认证已被驳回，请重新提交。' });
    }

    return ok(res, null, `实名认证已${action === 'approve' ? '通过' : '驳回'}`);
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

router.get(
  '/users/profile-reviews',
  asyncHandler(async (req, res) => {
    const users = await prisma.user.findMany({
      where: { profileReviewStatus: 'MANUAL_REVIEWING' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        avatar: true,
        pendingNickname: true,
        pendingAvatar: true,
        profileReviewNote: true,
        updatedAt: true,
      },
      orderBy: { updatedAt: 'desc' },
    });
    return ok(res, users);
  })
);

router.post(
  '/users/:userId/audit-profile',
  asyncHandler(async (req, res) => {
    const { userId } = req.params;
    const { action, note } = z.object({
      action: z.enum(['approve', 'reject']),
      note: z.string().optional()
    }).parse(req.body);

    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) throw new ApiError(404, '用户不存在');
    if (user.profileReviewStatus !== 'MANUAL_REVIEWING') {
      throw new ApiError(400, '用户无需进行资料审核');
    }

    if (action === 'approve') {
      await prisma.user.update({
        where: { id: userId },
        data: {
          nickname: user.pendingNickname || user.nickname,
          avatar: user.pendingAvatar || user.avatar,
          pendingNickname: null,
          pendingAvatar: null,
          profileReviewStatus: 'APPROVED',
          profileReviewNote: note || '人工审核通过',
        }
      });
    } else {
      await prisma.user.update({
        where: { id: userId },
        data: {
          pendingNickname: null,
          pendingAvatar: null,
          profileReviewStatus: 'REJECTED',
          profileReviewNote: note || '人工审核拒绝，包含违规信息',
        }
      });
    }

    return ok(res, null, `资料审核已${action === 'approve' ? '通过' : '拒绝'}`);
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

// ================= 商家认证审核 =================
router.get(
  '/merchants/certify',
  asyncHandler(async (req, res) => {
    const { status } = req.query;
    const dbStatus = status === 'PENDING' ? 'PENDING_REVIEW' : (status ? String(status) : undefined);
    const certs = await prisma.merchantCertification.findMany({
      where: dbStatus ? { status: dbStatus as any } : undefined,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { phone: true, nickname: true, avatar: true } }
      }
    });

    return ok(res, certs.map(c => ({
      ...c,
      storePhotos: JSON.parse(c.storePhotos || '[]')
    })));
  })
);

router.post(
  '/merchants/certify/:id/audit',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { action, reason } = z.object({
      action: z.enum(['approve', 'reject']),
      reason: z.string().optional()
    }).parse(req.body);

    const cert = await prisma.merchantCertification.findUnique({ where: { id } });
    if (!cert) throw new ApiError(404, '申请不存在');
    if (cert.status !== 'PENDING_REVIEW') throw new ApiError(400, '当前状态无需审核');

    if (action === 'approve') {
      // 1. Update cert status
      await prisma.merchantCertification.update({
        where: { id },
        data: { status: 'APPROVED' }
      });
      // 2. Change user role
      await prisma.user.update({
        where: { id: cert.userId },
        data: { role: 'MERCHANT_VERIFIED' }
      });
      // 3. Create or update Merchant / MerchantInfo
      const existingMerchant = await prisma.merchant.findFirst({ where: { ownerId: cert.userId } });
      if (!existingMerchant) {
        await prisma.merchant.create({
          data: {
            ownerId: cert.userId,
            name: cert.storeName,
            logo: cert.storePhotos.length > 0 ? JSON.parse(cert.storePhotos)[0] : '',
            banner: '',
            category: cert.categoryId,
            province: '',
            city: '',
            district: '',
            town: '',
            description: '新入驻商家',
            address: '',
            phone: cert.contactPhone
          }
        });
      }
      
      const existingInfo = await prisma.merchantInfo.findUnique({ where: { userId: cert.userId }});
      if (!existingInfo) {
        await prisma.merchantInfo.create({
          data: {
            userId: cert.userId,
            shopName: cert.storeName,
            address: '',
            contactPhone: cert.contactPhone,
            verifyStatus: 'APPROVED'
          }
        });
      } else {
        await prisma.merchantInfo.update({
          where: { userId: cert.userId },
          data: { verifyStatus: 'APPROVED' }
        });
      }
      
      return ok(res, null, '商家入驻申请已通过');
    } else {
      await prisma.merchantCertification.update({
        where: { id },
        data: { status: 'REJECTED', rejectReason: reason || '不符合要求' }
      });
      return ok(res, null, '商家入驻申请已驳回');
    }
  })
);

// ================= 数据治理与主动风控 (Active Governance) =================
router.get(
  '/governance/search-users',
  asyncHandler(async (req, res) => {
    const { keyword } = z.object({ keyword: z.string().min(1) }).parse(req.query);
    
    // Attempt to search by exact ID, phone, or nickname
    const users = await prisma.user.findMany({
      where: {
        OR: [
          { id: keyword },
          { phone: { contains: keyword } },
          { nickname: { contains: keyword } }
        ]
      },
      take: 20,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        avatar: true,
        status: true,
        role: true,
        realNameStatus: true,
      }
    });

    return ok(res, users);
  })
);

router.get(
  '/governance/search-posts',
  asyncHandler(async (req, res) => {
    const { keyword } = z.object({ keyword: z.string().min(1) }).parse(req.query);
    
    const posts = await prisma.post.findMany({
      where: {
        OR: [
          { id: keyword },
          { title: { contains: keyword } },
          { description: { contains: keyword } }
        ]
      },
      take: 20,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        title: true,
        description: true,
        status: true,
        createdAt: true,
        user: {
          select: { id: true, nickname: true, phone: true }
        }
      }
    });

    return ok(res, posts);
  })
);

router.post(
  '/governance/ban-user',
  asyncHandler(async (req, res) => {
    // req.userId is set by requireAdminAuth middleware
    const adminId = req.userId!;
    const { targetUserId, reason } = z.object({
      targetUserId: z.string(),
      reason: z.string().optional()
    }).parse(req.body);

    await prisma.$transaction(async (tx) => {
      // 1. 封禁用户账号
      await tx.user.update({
        where: { id: targetUserId },
        data: { status: 'banned' }
      });
      
      // 2. 级联下架该用户所有展示中的商品/帖子
      await tx.post.updateMany({
        where: { userId: targetUserId, status: 'published' },
        data: { status: 'taken_down' }
      });

      // 3. 记录审计日志
      await tx.adminAuditLog.create({
        data: {
          adminId,
          targetId: targetUserId,
          actionType: 'BAN_USER',
          reason: reason || '平台合规风控封禁'
        }
      });
    });

    // 4. WebSocket 通知强踢和全网刷新
    pushToUser(targetUserId, { type: 'FORCE_LOGOUT', reason: reason || '平台合规风控封禁' });
    broadcastToAll({ type: 'DATA_REFRESH_REQUIRED', target: 'posts' });

    return ok(res, null, 'User banned and resources taken down.');
  })
);

router.post(
  '/governance/revoke-post',
  asyncHandler(async (req, res) => {
    const adminId = req.userId!;
    const { targetPostId, reason } = z.object({
      targetPostId: z.string(),
      reason: z.string().optional()
    }).parse(req.body);

    await prisma.$transaction(async (tx) => {
      await tx.post.update({
        where: { id: targetPostId },
        data: { status: 'removed', reviewNote: reason || '超级管理员强制下架' }
      });
      await tx.adminAuditLog.create({
        data: {
          adminId,
          targetId: targetPostId,
          actionType: 'TAKE_DOWN_POST',
          reason: reason || '超级管理员强制下架'
        }
      });
    });

    broadcastToAll({ type: 'DATA_REFRESH_REQUIRED', target: 'posts' });
    return ok(res, null, 'Post removed successfully.');
  })
);

router.post(
  '/governance/revoke-kyc',
  asyncHandler(async (req, res) => {
    const adminId = req.userId!;
    const { targetUserId, reason } = z.object({
      targetUserId: z.string(),
      reason: z.string().optional()
    }).parse(req.body);

    await prisma.$transaction(async (tx) => {
      await tx.user.update({
        where: { id: targetUserId },
        data: { realNameStatus: 'none', realName: null, idCardHash: null }
      });
      await tx.adminAuditLog.create({
        data: {
          adminId,
          targetId: targetUserId,
          actionType: 'REVOKE_REAL_NAME',
          reason: reason || '虚假实名认证撤销'
        }
      });
    });
    pushToUser(targetUserId, { event: 'USER_STATE_CHANGED', message: '您的实名认证因违规已被撤销。' });
    return ok(res, null, 'User KYC revoked successfully.');
  })
);

router.post(
  '/governance/revoke-merchant',
  asyncHandler(async (req, res) => {
    const adminId = req.userId!;
    const { targetUserId, reason } = z.object({
      targetUserId: z.string(),
      reason: z.string().optional()
    }).parse(req.body);

    await prisma.$transaction(async (tx) => {
      const user = await tx.user.findUnique({ where: { id: targetUserId }});
      if (!user) throw new ApiError(404, 'User not found');
      
      const newRole = user.role.replace('MERCHANT_VERIFIED', 'USER');
      await tx.user.update({
        where: { id: targetUserId },
        data: { role: newRole }
      });
      
      await tx.merchantInfo.updateMany({
        where: { userId: targetUserId },
        data: { verifyStatus: 'REJECTED' }
      });
      
      await tx.merchantCertification.updateMany({
        where: { userId: targetUserId, status: 'APPROVED' },
        data: { status: 'REJECTED', rejectReason: reason || '虚假/违规商家认证撤销' }
      });

      await tx.adminAuditLog.create({
        data: {
          adminId,
          targetId: targetUserId,
          actionType: 'REVOKE_MERCHANT',
          reason: reason || '虚假/违规商家认证撤销'
        }
      });
    });
    pushToUser(targetUserId, { event: 'USER_STATE_CHANGED', message: '您的商家入驻认证因违规已被撤销。' });
    return ok(res, null, 'Merchant auth revoked successfully.');
  })
);

router.get(
  '/governance/search-users',
  asyncHandler(async (req, res) => {
    const keyword = (req.query.keyword as string) || '';
    if (!keyword.trim()) return ok(res, []);

    const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(keyword);
    const where: any = {
      OR: [
        { phone: { contains: keyword } },
        { nickname: { contains: keyword } }
      ]
    };
    if (isUuid) {
      where.OR.push({ id: keyword });
    }

    const users = await prisma.user.findMany({
      where,
      take: 20,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        avatar: true,
        status: true,
        realNameStatus: true,
        role: true,
        createdAt: true
      }
    });

    return ok(res, users);
  })
);

router.get(
  '/governance/search-posts',
  asyncHandler(async (req, res) => {
    const keyword = (req.query.keyword as string) || '';
    if (!keyword.trim()) return ok(res, []);

    const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(keyword);
    const where: any = {
      OR: [
        { title: { contains: keyword } },
        { description: { contains: keyword } }
      ]
    };
    if (isUuid) {
      where.OR.push({ id: keyword });
    }

    const posts = await prisma.post.findMany({
      where,
      take: 20,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        title: true,
        description: true,
        status: true,
        createdAt: true,
        user: { select: { nickname: true, phone: true } }
      }
    });

    return ok(res, posts);
  })
);

export default router;
