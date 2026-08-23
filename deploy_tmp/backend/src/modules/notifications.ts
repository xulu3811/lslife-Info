import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();
router.use(requireAuth);

router.get(
  '/',
  asyncHandler(async (req, res) => {
    const list = await prisma.notification.findMany({ where: { userId: req.userId! }, orderBy: { createdAt: 'desc' }, take: 100 });
    const unread = list.filter((n) => !n.read).length;
    return ok(res, { list, unread });
  }),
);

router.post(
  '/read-all',
  asyncHandler(async (req, res) => {
    await prisma.notification.updateMany({ where: { userId: req.userId!, read: false }, data: { read: true } });
    return ok(res, { done: true });
  }),
);

router.post(
  '/:id/read',
  asyncHandler(async (req, res) => {
    await prisma.notification.updateMany({ where: { id: req.params.id, userId: req.userId! }, data: { read: true } });
    return ok(res, { done: true });
  }),
);

router.delete(
  '/',
  asyncHandler(async (req, res) => {
    await prisma.notification.deleteMany({ where: { userId: req.userId! } });
    return ok(res, { cleared: true });
  }),
);

export default router;
