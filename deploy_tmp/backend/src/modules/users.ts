import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';

const router = Router();

/**
 * 获取公开的用户资料（用于 C 端主页）
 * 返回头像、昵称、认证状态等公开信息
 */
router.get(
  '/:id/public',
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
            status: true
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

    const responseUser = {
        id: user.id,
        nickname: user.nickname,
        avatar: user.avatar,
        identityType: user.identityType,
        createdAt: user.createdAt,
        isMerchant,
        authLabel
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
      take: 20, // Return first 20 for showcase
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

export default router;
