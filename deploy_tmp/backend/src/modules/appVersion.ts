import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAdminAuth } from '../middleware/auth.js';

const router = Router();

// ==================== 公开接口 ====================

/**
 * GET /api/app/version
 * 公开接口 — 返回当前激活的最新版本信息（isActive=true, versionCode 最大）
 * 客户端启动时调用，无需鉴权
 */
router.get(
  '/version',
  asyncHandler(async (_req, res) => {
    const version = await prisma.appVersion.findFirst({
      where: { isActive: true },
      orderBy: { versionCode: 'desc' },
    });
    if (!version) {
      return ok(res, null, '暂无可用版本信息');
    }
    return ok(res, version, '获取成功');
  }),
);

// ==================== 管理员接口 ====================

/** 以下接口均需管理员 JWT */
router.use(requireAdminAuth);

/**
 * GET /api/admin/app-versions
 * 历史版本列表（分页）
 */
router.get(
  '/versions',
  asyncHandler(async (req, res) => {
    const { page = '1', limit = '20' } = req.query as Record<string, string>;
    const skip = (Number(page) - 1) * Number(limit);

    const [versions, total] = await Promise.all([
      prisma.appVersion.findMany({
        orderBy: { versionCode: 'desc' },
        skip,
        take: Number(limit),
      }),
      prisma.appVersion.count(),
    ]);

    return ok(res, { versions, total, page: Number(page), limit: Number(limit) });
  }),
);

/**
 * POST /api/admin/app-version
 * 发布新版本
 */
router.post(
  '/version',
  asyncHandler(async (req, res) => {
    const schema = z.object({
      versionName: z.string().min(1),
      versionCode: z.number().int().positive(),
      downloadUrl: z.string().url(),
      releaseNotes: z.string().min(1),
      isForced: z.boolean().default(false),
      isActive: z.boolean().default(true),
      fileSize: z.number().int().positive().optional(),
      md5: z.string().optional(),
    });

    const data = schema.parse(req.body);

    // 若新版本标记为激活，将同 versionCode 级别以下的旧激活版本置为非激活
    // 注：此处只停用 versionCode 更低的已激活版本，管理员可手动切换
    if (data.isActive) {
      await prisma.appVersion.updateMany({
        where: {
          isActive: true,
          versionCode: { lt: data.versionCode },
        },
        data: { isActive: false },
      });
    }

    const version = await prisma.appVersion.create({ data });
    return ok(res, version, '版本发布成功');
  }),
);

/**
 * PATCH /api/admin/app-versions/:id
 * 切换激活版本 / 修改强制标志 / 修改更新日志等
 */
router.patch(
  '/versions/:id',
  asyncHandler(async (req, res) => {
    const { id } = req.params;

    const schema = z.object({
      isActive: z.boolean().optional(),
      isForced: z.boolean().optional(),
      releaseNotes: z.string().optional(),
      downloadUrl: z.string().url().optional(),
    });

    const data = schema.parse(req.body);

    // 若要激活某版本，先把所有版本都置为非激活
    if (data.isActive === true) {
      await prisma.appVersion.updateMany({
        where: { isActive: true },
        data: { isActive: false },
      });
    }

    const version = await prisma.appVersion.update({
      where: { id },
      data,
    });
    return ok(res, version, '更新成功');
  }),
);

/**
 * DELETE /api/admin/app-versions/:id
 * 删除版本记录（谨慎使用）
 */
router.delete(
  '/versions/:id',
  asyncHandler(async (req, res) => {
    const { id } = req.params;

    const existing = await prisma.appVersion.findUnique({ where: { id } });
    if (!existing) throw new ApiError(404, '版本不存在');
    if (existing.isActive) throw new ApiError(400, '无法删除当前激活版本，请先切换其他版本');

    await prisma.appVersion.delete({ where: { id } });
    return ok(res, null, '删除成功');
  }),
);

export default router;
