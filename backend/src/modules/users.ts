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

export default router;
