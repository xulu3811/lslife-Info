import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { requireAuth } from '../middleware/auth.js';
import { ok, fail } from '../lib/http.js';

const router = Router();

// 发送好友请求
router.post('/request', requireAuth, async (req, res) => {
  const { friendId, message } = req.body;
  const userId = req.userId!;

  if (userId === friendId) {
    return fail(res, 400, '不能添加自己为好友');
  }

  try {
    // 检查是否已经是好友
    const existingFriend = await prisma.friendship.findUnique({
      where: { userId_friendId: { userId, friendId } }
    });
    if (existingFriend) {
      return fail(res, 400, '已经是好友了');
    }

    // 检查是否已发送请求
    const existingRequest = await prisma.friendRequest.findFirst({
      where: { senderId: userId, receiverId: friendId, status: 'PENDING' }
    });
    if (existingRequest) {
      return fail(res, 400, '已发送过好友请求，请等待对方同意');
    }

    const request = await prisma.friendRequest.create({
      data: { senderId: userId, receiverId: friendId, message }
    });

    ok(res, { request });
  } catch (error) {
    console.error(error);
    fail(res, 400, '发送请求失败');
  }
});

// 处理好友请求 (同意/拒绝)
router.post('/handle', requireAuth, async (req, res) => {
  const { requestId, action } = req.body; // action: 'ACCEPT' or 'REJECT'
  const userId = req.userId!;

  try {
    const request = await prisma.friendRequest.findUnique({ where: { id: requestId } });
    if (!request || request.receiverId !== userId) {
      return fail(res, 400, '请求不存在或无权处理');
    }
    if (request.status !== 'PENDING') {
      return fail(res, 400, '请求已处理');
    }

    if (action === 'ACCEPT') {
      await prisma.$transaction([
        prisma.friendRequest.update({
          where: { id: requestId },
          data: { status: 'ACCEPTED' }
        }),
        prisma.friendship.create({
          data: { userId: request.senderId, friendId: request.receiverId }
        }),
        prisma.friendship.create({
          data: { userId: request.receiverId, friendId: request.senderId }
        })
      ]);
      return ok(res, { message: '已添加为好友' });
    } else if (action === 'REJECT') {
      await prisma.friendRequest.update({
        where: { id: requestId },
        data: { status: 'REJECTED' }
      });
      return ok(res, { message: '已拒绝' });
    } else {
      return fail(res, 400, '未知操作');
    }
  } catch (error) {
    console.error(error);
    fail(res, 400, '处理请求失败');
  }
});

// 获取好友列表
router.get('/list', requireAuth, async (req, res) => {
  const userId = req.userId!;
  try {
    const friends = await prisma.friendship.findMany({
      where: { userId },
      include: {
        friend: {
          select: {
            id: true,
            nickname: true,
            avatar: true,
            status: true
          }
        }
      }
    });
    ok(res, { friends: friends.map(f => f.friend) });
  } catch (error) {
    console.error(error);
    fail(res, 400, '获取好友列表失败');
  }
});

// 获取收到的好友请求
router.get('/pending', requireAuth, async (req, res) => {
  const userId = req.userId!;
  try {
    const requests = await prisma.friendRequest.findMany({
      where: { receiverId: userId, status: 'PENDING' },
      include: {
        sender: {
          select: {
            id: true,
            nickname: true,
            avatar: true
          }
        }
      },
      orderBy: { createdAt: 'desc' }
    });
    ok(res, { requests });
  } catch (error) {
    console.error(error);
    fail(res, 400, '获取好友请求失败');
  }
});

export default router;
