import { Router } from 'express';
import { z } from 'zod';
import { customAlphabet } from 'nanoid';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { computeLiveDelivery } from '../services/delivery.js';

const router = Router();
router.use(requireAuth);

const genOrderNo = customAlphabet('0123456789', 6);

/** 创建订单 (服务端计算金额, 防止客户端篡改) */
router.post(
  '/',
  asyncHandler(async (req, res) => {
    const { merchantId, items, deliveryAddress } = z
      .object({
        merchantId: z.string(),
        items: z.array(z.object({ productId: z.string(), quantity: z.number().int().positive() })).min(1),
        deliveryAddress: z.object({ name: z.string(), phone: z.string(), address: z.string() }),
      })
      .parse(req.body);

    const merchant = await prisma.merchant.findFirst({ where: { OR: [{ id: merchantId }, { externalId: merchantId }] } });
    if (!merchant) throw new ApiError(400, '商家不存在');

    const productIds = items.map((i) => i.productId);
    const products = await prisma.product.findMany({ where: { id: { in: productIds } } });
    if (products.length !== items.length) throw new ApiError(400, '存在无效商品');

    let itemsTotal = 0;
    const orderItemsData = items.map((i) => {
      const p = products.find((x) => x.id === i.productId)!;
      if (p.stock < i.quantity) throw new ApiError(400, `商品【${p.name}】库存不足`);
      itemsTotal += p.price * i.quantity;
      return { productId: p.id, name: p.name, price: p.price, quantity: i.quantity, image: p.image };
    });
    const totalAmount = itemsTotal + merchant.deliveryFee;

    const order = await prisma.$transaction(async (tx) => {
      const created = await tx.order.create({
        data: {
          orderNo: 'LS' + genOrderNo(),
          userId: req.userId!,
          merchantId: merchant.id,
          merchantName: merchant.name,
          merchantLogo: merchant.logo,
          itemsTotal,
          deliveryFee: merchant.deliveryFee,
          totalAmount,
          status: 'pending',
          deliveryName: deliveryAddress.name,
          deliveryPhone: deliveryAddress.phone,
          deliveryAddress: deliveryAddress.address,
          items: { create: orderItemsData },
        },
        include: { items: true },
      });

      for (const item of items) {
        await tx.product.update({
          where: { id: item.productId },
          data: { stock: { decrement: item.quantity } },
        });
      }
      return created;
    });

    return ok(res, order, '订单创建成功, 待支付');
  }),
);

/** 订单列表 */
router.get(
  '/',
  asyncHandler(async (req, res) => {
    const orders = await prisma.order.findMany({
      where: { userId: req.userId! },
      orderBy: { createdAt: 'desc' },
      include: { items: true },
    });
    return ok(res, orders);
  }),
);

/** 订单详情 + 实时配送 */
router.get(
  '/:id',
  asyncHandler(async (req, res) => {
    const order = await prisma.order.findFirst({
      where: { id: req.params.id, userId: req.userId! },
      include: { items: true, merchant: true, payment: true },
    });
    if (!order) throw new ApiError(404, '订单不存在');

    let delivery = null;
    if (['paid', 'preparing', 'delivering', 'delivered'].includes(order.status)) {
      delivery = await computeLiveDelivery(order.id);
    }
    return ok(res, { ...order, delivery });
  }),
);

/** 取消订单 (仅未支付) */
router.post(
  '/:id/cancel',
  asyncHandler(async (req, res) => {
    const order = await prisma.order.findFirst({ 
      where: { id: req.params.id, userId: req.userId! },
      include: { items: true },
    });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status !== 'pending') throw new ApiError(400, '当前订单状态不可取消');
    
    const updated = await prisma.$transaction(async (tx) => {
      const cancelled = await tx.order.update({ where: { id: order.id }, data: { status: 'cancelled' } });
      for (const item of order.items) {
        await tx.product.update({
          where: { id: item.productId },
          data: { stock: { increment: item.quantity } },
        });
      }
      return cancelled;
    });
    
    return ok(res, updated, '已取消');
  }),
);

export default router;
