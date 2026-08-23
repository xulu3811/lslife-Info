import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { z } from 'zod';

const router = Router();

/**
 * 获取公开的用户资料（用于 C 端主页）
 * 返回头像、昵称、认证状态等公开信息
 */
router.get(
  '/:id/public',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { id } = req.params;
    
    const user = await prisma.user.findUnique({
      where: { id },
      select: {
        id: true,
        nickname: true,
        avatar: true,
        identityType: true,
        realNameStatus: true,
        createdAt: true,
        merchantCertification: {
          select: {
            status: true,
            businessLicenseUrl: true,
            storeName: true,
            certType: true
          }
        }
      },
    });

    if (!user) {
      throw new ApiError(404, '用户不存在或已注销');
    }

    const isMerchant = !!user.merchantCertification && user.merchantCertification.status === 'APPROVED';
    let authLabel = '普通用户';
    if (isMerchant) {
        authLabel = '认证商家';
    } else if (user.realNameStatus === 'verified') {
        authLabel = '认证个人用户';
    }

    let isFollowing = false;
    if (req.userId) {
      const follow = await prisma.follow.findUnique({
        where: { followerId_followingId: { followerId: req.userId, followingId: user.id } }
      });
      isFollowing = !!follow;
    }

    const responseUser = {
        id: user.id,
        nickname: user.nickname,
        avatar: user.avatar,
        identityType: user.identityType,
        createdAt: user.createdAt,
        isMerchant,
        authLabel,
        isFollowing,
        businessLicenseUrl: isMerchant ? (user.merchantCertification?.businessLicenseUrl || null) : null,
        storeName: isMerchant ? (user.merchantCertification?.storeName || null) : null,
        certType: isMerchant ? (user.merchantCertification?.certType || null) : null
    };

    return ok(res, responseUser, '获取公开用户信息成功');
  }),
);

/**
 * 搜索用户 (按手机号或昵称)
 */
router.get(
  '/search',
  asyncHandler(async (req, res) => {
    const { q } = req.query;
    if (!q || typeof q !== 'string') {
      return ok(res, [], '请输入搜索词');
    }

    const users = await prisma.user.findMany({
      where: {
        OR: [
          { phone: q },
          { nickname: { contains: q } }
        ]
      },
      select: {
        id: true,
        nickname: true,
        avatar: true,
        identityType: true,
        createdAt: true
      },
      take: 20
    });

    return ok(res, users, '搜索成功');
  })
);

/**
 * 个人/商家橱窗展示 (C 端/商家主页)
 */
router.get(
  '/:id/showcase',
  asyncHandler(async (req, res) => {
    const { id } = req.params;

    const user = await prisma.user.findUnique({
      where: { id },
      select: {
        id: true,
        nickname: true,
        avatar: true,
        role: true,
        createdAt: true,
        merchantInfo: true,
      },
    });

    if (!user) {
      throw new ApiError(404, '用户不存在');
    }

    const posts = await prisma.post.findMany({
      where: { userId: id, status: 'published' },
      orderBy: [
        { isTop: 'desc' },
        { refreshedAt: 'desc' },
        { createdAt: 'desc' }
      ],
      take: 20,
    });

    return ok(res, {
      profile: user,
      posts: posts.map((p) => ({
        ...p,
        images: JSON.parse(p.images) as string[],
        attributes: JSON.parse(p.attributes) as Record<string, string>,
      })),
    }, '获取橱窗信息成功');
  })
);

/** 关注 / 取关用户 */
router.post(
  '/:id/follow',
  requireAuth,
  asyncHandler(async (req, res) => {
    const followingId = req.params.id;
    const followerId = req.userId!;
    
    if (followingId === followerId) {
      throw new ApiError(400, '不能关注自己');
    }

    const targetUser = await prisma.user.findUnique({ where: { id: followingId } });
    if (!targetUser) throw new ApiError(404, '目标用户不存在');

    const existingFollow = await prisma.follow.findUnique({
      where: { followerId_followingId: { followerId, followingId } }
    });

    let isFollowing = false;

    if (existingFollow) {
      await prisma.$transaction([
        prisma.follow.delete({ where: { id: existingFollow.id } }),
        prisma.user.update({ where: { id: followerId }, data: { followingCount: { decrement: 1 } } }),
        prisma.user.update({ where: { id: followingId }, data: { followersCount: { decrement: 1 } } })
      ]);
    } else {
      await prisma.$transaction([
        prisma.follow.create({ data: { followerId, followingId } }),
        prisma.user.update({ where: { id: followerId }, data: { followingCount: { increment: 1 } } }),
        prisma.user.update({ where: { id: followingId }, data: { followersCount: { increment: 1 } } })
      ]);
      isFollowing = true;
    }

    return ok(res, { isFollowing }, isFollowing ? '关注成功' : '已取消关注');
  })
);

/** 获取粉丝列表 */
router.get(
  '/:id/followers',
  asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { page, pageSize } = z
      .object({
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
      })
      .parse(req.query);

    const followers = await prisma.follow.findMany({
      where: { followingId: id },
      orderBy: { createdAt: 'desc' },
      skip: (page - 1) * pageSize,
      take: pageSize,
      include: {
        follower: {
          select: {
            id: true,
            nickname: true,
            avatar: true,
            identityType: true,
            realNameStatus: true,
            merchantCertification: { select: { status: true } }
          }
        }
      }
    });

    const total = await prisma.follow.count({ where: { followingId: id } });

    const list = followers.map(f => {
      const u = f.follower;
      const isMerchant = !!u.merchantCertification && u.merchantCertification.status === 'APPROVED';
      let authLabel = '普通用户';
      if (isMerchant) authLabel = '认证商家';
      else if (u.realNameStatus === 'verified') authLabel = '认证个人用户';
      return {
        id: u.id,
        nickname: u.nickname,
        avatar: u.avatar,
        identityType: authLabel,
        authLabel: authLabel
      };
    });

    return ok(res, { list, total, page, pageSize });
  })
);

/** 获取关注列表 */
router.get(
  '/:id/following',
  asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { page, pageSize } = z
      .object({
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
      })
      .parse(req.query);

    const following = await prisma.follow.findMany({
      where: { followerId: id },
      orderBy: { createdAt: 'desc' },
      skip: (page - 1) * pageSize,
      take: pageSize,
      include: {
        following: {
          select: {
            id: true,
            nickname: true,
            avatar: true,
            identityType: true,
            realNameStatus: true,
            merchantCertification: { select: { status: true } }
          }
        }
      }
    });

    const total = await prisma.follow.count({ where: { followerId: id } });

    const list = following.map(f => {
      const u = f.following;
      const isMerchant = !!u.merchantCertification && u.merchantCertification.status === 'APPROVED';
      let authLabel = '普通用户';
      if (isMerchant) authLabel = '认证商家';
      else if (u.realNameStatus === 'verified') authLabel = '认证个人用户';
      return {
        id: u.id,
        nickname: u.nickname,
        avatar: u.avatar,
        identityType: authLabel,
        authLabel: authLabel
      };
    });

    return ok(res, { list, total, page, pageSize });
  })
);

export default router;
