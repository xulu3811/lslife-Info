import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();
router.use(requireAuth);

const addressSchema = z.object({
  name: z.string().min(1),
  phone: z.string().regex(/^1\d{10}$/, '手机号格式错误'),
  tag: z.enum(['家', '公司', '学校', '其他']).default('家'),
  address: z.string().min(1),
  latitude: z.number().optional(),
  longitude: z.number().optional(),
  isDefault: z.boolean().default(false),
});

router.get(
  '/',
  asyncHandler(async (req, res) => {
    const list = await prisma.address.findMany({ where: { userId: req.userId! }, orderBy: [{ isDefault: 'desc' }, { createdAt: 'desc' }] });
    return ok(res, list);
  }),
);

router.post(
  '/',
  asyncHandler(async (req, res) => {
    const body = addressSchema.parse(req.body);
    if (body.isDefault) {
      await prisma.address.updateMany({ where: { userId: req.userId! }, data: { isDefault: false } });
    }
    const count = await prisma.address.count({ where: { userId: req.userId! } });
    const address = await prisma.address.create({ data: { ...body, userId: req.userId!, isDefault: body.isDefault || count === 0 } });
    return ok(res, address, '已添加');
  }),
);

router.put(
  '/:id',
  asyncHandler(async (req, res) => {
    const body = addressSchema.parse(req.body);
    const existing = await prisma.address.findFirst({ where: { id: req.params.id, userId: req.userId! } });
    if (!existing) throw new ApiError(404, '地址不存在');
    if (body.isDefault) {
      await prisma.address.updateMany({ where: { userId: req.userId! }, data: { isDefault: false } });
    }
    const address = await prisma.address.update({ where: { id: req.params.id }, data: body });
    return ok(res, address, '已更新');
  }),
);

router.delete(
  '/:id',
  asyncHandler(async (req, res) => {
    const existing = await prisma.address.findFirst({ where: { id: req.params.id, userId: req.userId! } });
    if (!existing) throw new ApiError(404, '地址不存在');
    await prisma.address.delete({ where: { id: req.params.id } });
    if (existing.isDefault) {
      const next = await prisma.address.findFirst({ where: { userId: req.userId! } });
      if (next) await prisma.address.update({ where: { id: next.id }, data: { isDefault: true } });
    }
    return ok(res, { deleted: true });
  }),
);

export default router;
