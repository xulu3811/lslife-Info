import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { Prisma } from '@prisma/client';
import { ok } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { moderateContent } from '../services/moderation.js';

const router = Router();

// Configure validity periods based on category
const DURATION_MAP: Record<string, number> = {
  emergency: 3, // 3 days (24-72 hours)
  gossip: 7,    // 7 days
  idle: 30,     // 30 days
  part_time: 15,
};

router.post(
  '/',
  requireAuth,
  asyncHandler(async (req, res) => {
    const body = z
      .object({
        category: z.string(),
        content: z.string().min(1).max(1000),
        images: z.array(z.string()).max(9).default([]),
        province: z.string().max(40).optional(),
        city: z.string().max(40).optional(),
        district: z.string().max(40).optional(),
        town: z.string().max(60).optional(),
        streetAddress: z.string().max(200).optional(),
        userRequestedDuration: z.number().optional() // e.g. 3 or 7
      })
      .parse(req.body);

    const userId = (req as any).user!.id;

    // Default duration map logic
    const defaultDays = DURATION_MAP[body.category] || 7;
    const finalDays = body.userRequestedDuration || defaultDays;
    
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + finalDays);

    // AI Semantic review (DFA / DeepSeek check)
    let status = 'AI_REVIEWING';
    let evidenceHash = null;

    try {
      const moderationResult = await moderateContent(body.category, body.content);
      if (moderationResult.pass) {
        status = 'PUBLISHED';
      } else {
        status = 'MANUAL_REVIEWING';
      }
    } catch (e) {
      // Fallback
      status = 'PUBLISHED';
    }

    const dynamicPost = await prisma.dynamicPost.create({
      data: {
        userId,
        content: body.content,
        images: JSON.stringify(body.images),
        province: body.province,
        city: body.city,
        district: body.district,
        town: body.town,
        streetAddress: body.streetAddress,
        category: body.category,
        status,
        expiresAt,
        evidenceHash
      }
    });

    ok(res, { dynamicPost });
  })
);


router.get(
  '/',
  asyncHandler(async (req, res) => {
    const { category, page = '1', pageSize = '20', province, city, district, town } = req.query;
    const skip = (Number(page) - 1) * Number(pageSize);
    const take = Number(pageSize);

    const where: any = {
      status: 'PUBLISHED',
      expiresAt: { gt: new Date() }
    };
    if (category) where.category = String(category);
    if (province) where.province = String(province);
    if (city) where.city = String(city);
    if (district) where.district = String(district);
    if (town) where.town = String(town);

    const [list, total] = await Promise.all([
      prisma.dynamicPost.findMany({
        where,
        skip,
        take,
        orderBy: { createdAt: 'desc' },
        include: { user: { select: { id: true, nickname: true, avatar: true } } }
      }),
      prisma.dynamicPost.count({ where })
    ]);

    ok(res, { list, total, page: Number(page), pageSize: take });
  })
);

export default router;
