import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { pushToUser } from '../realtime/hub.js';

const router = Router();

// MVP 阶段骑手伪装 Token
const RIDER_TEST_TOKEN = 'RIDER_TEST_TOKEN';

// 简易拦截器: 检查是否为合法骑手 Token
const requireRider = (req: any, _res: any, next: any) => {
  const auth = req.headers.authorization;
  if (!auth || auth !== `Bearer ${RIDER_TEST_TOKEN}`) {
    throw new ApiError(401, 'Unauthorized rider');
  }
  next();
};

/** 骑手上报真实坐标 (并自动推给下单用户) */
router.post(
  '/location',
  requireRider,
  asyncHandler(async (req, res) => {
    const { orderId, lat, lng } = z
      .object({
        orderId: z.string(),
        lat: z.number(),
        lng: z.number(),
      })
      .parse(req.body);

    const delivery = await prisma.delivery.findUnique({
      where: { orderId },
      include: { order: true },
    });

    if (!delivery) throw new ApiError(404, '配送单不存在');

    // 更新坐标及状态
    const updated = await prisma.delivery.update({
      where: { orderId },
      data: {
        riderLat: lat,
        riderLng: lng,
        status: 'delivering',
      },
      include: { order: true },
    });
    
    // 同步更新订单状态
    if (updated.order.status === 'preparing' || updated.order.status === 'paid') {
      await prisma.order.update({
        where: { id: orderId },
        data: { status: 'delivering' },
      });
    }

    // 触发 WebSocket 主动推送给下单用户
    pushToUser(updated.order.userId, {
      event: 'rider_location',
      orderId,
      lat,
      lng,
      status: updated.status,
    });

    return ok(res, { success: true, riderLat: lat, riderLng: lng }, '坐标上报成功');
  })
);

/** 骑手确认送达 */
router.post(
  '/deliver',
  requireRider,
  asyncHandler(async (req, res) => {
    const { orderId } = z.object({ orderId: z.string() }).parse(req.body);

    const delivery = await prisma.delivery.update({
      where: { orderId },
      data: { status: 'delivered', progress: 100 },
      include: { order: true },
    });

    await prisma.order.update({
      where: { id: orderId },
      data: { status: 'delivered' },
    });

    pushToUser(delivery.order.userId, {
      event: 'order_delivered',
      orderId,
    });

    return ok(res, { success: true }, '订单已成功送达');
  })
);

export default router;
