import type { NextFunction, Request, Response } from 'express';
import { verifyToken } from '../lib/jwt.js';
import { ApiError } from '../lib/http.js';
import { prisma } from '../lib/prisma.js';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Express {
    interface Request {
      userId?: string;
      userPhone?: string;
      isAdmin?: boolean;
      adminRole?: string;
    }
  }
}

/** 强制鉴权: 无有效 token 返回 401 */
export function requireAuth(req: Request, _res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) {
    return next(new ApiError(401, '未登录或登录已过期'));
  }
  try {
    const payload = verifyToken(header.slice(7));
    req.userId = payload.sub;
    req.userPhone = payload.phone;
    next();
  } catch (err) {
    console.error('[requireAuth] error:', err);
    if (err instanceof ApiError) return next(err);
    return next(new ApiError(401, '登录凭证无效'));
  }
}

/** 管理后台鉴权: 必须 isAdmin，并校验 AdminUser 仍存在 */
export function requireAdminAuth(req: Request, _res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) {
    return next(new ApiError(401, '未登录或登录已过期'));
  }

  let payload;
  try {
    payload = verifyToken(header.slice(7));
  } catch {
    return next(new ApiError(401, '登录凭证无效'));
  }

  if (payload.role) {
    prisma.adminUser
      .findUnique({ where: { id: payload.sub } })
      .then((admin) => {
        if (!admin) {
          return next(new ApiError(403, '管理员账号不存在或已停用'));
        }
        req.userId = admin.id;
        req.isAdmin = true;
        req.adminRole = admin.role;
        next();
      })
      .catch(next);
  } else {
    // 允许 App 端的 ADMIN 和 SUPERADMIN 用户
    prisma.user
      .findUnique({ where: { id: payload.sub } })
      .then((user) => {
        if (user && (user.role === 'ADMIN' || user.role === 'SUPERADMIN')) {
          req.userId = user.id;
          req.isAdmin = true;
          req.adminRole = user.role;
          next();
        } else {
          return next(new ApiError(403, '需要管理员权限'));
        }
      })
      .catch(next);
  }
}

/** 可选鉴权: 有 token 则解析, 无则放行 */
export function optionalAuth(req: Request, _res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (header?.startsWith('Bearer ')) {
    try {
      const payload = verifyToken(header.slice(7));
      req.userId = payload.sub;
      req.userPhone = payload.phone;
    } catch {
      /* ignore */
    }
  }
  next();
}
