import express from 'express';
import { PrismaClient } from '@prisma/client';
import { requireAuth } from '../middleware/auth.js';
import { UserWalletService } from '../services/UserWalletService.js';

const router = express.Router();
const prisma = new PrismaClient();
const walletService = new UserWalletService();

// 获取今天的零点时间
const getTodayZero = () => {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  return now;
};

// 获取昨天的零点时间
const getYesterdayZero = () => {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  yesterday.setHours(0, 0, 0, 0);
  return yesterday;
};

// 获取签到状态
router.get('/status', requireAuth, async (req: any, res) => {
  const userId = req.userId;

  try {
    let record = await prisma.userSignInRecord.findUnique({
      where: { userId }
    });

    if (!record) {
      return res.json({
        is_signed_today: false,
        continuous_days: 0
      });
    }

    const todayZero = getTodayZero().getTime();
    const yesterdayZero = getYesterdayZero().getTime();
    const lastSignInTime = record.lastSignInDate?.getTime() ?? 0;

    let isSignedToday = false;
    let continuousDays = record.continuousDays;

    if (lastSignInTime >= todayZero) {
      isSignedToday = true;
    } else if (lastSignInTime < yesterdayZero) {
      // 昨天没签到，断签
      continuousDays = 0;
    }

    res.json({
      is_signed_today: isSignedToday,
      continuous_days: continuousDays
    });
  } catch (error) {
    console.error('Fetch signin status error:', error);
    res.status(500).json({ error: 'Failed to fetch signin status' });
  }
});

// 执行签到
router.post('/execute', requireAuth, async (req: any, res) => {
  const userId = req.userId;

  try {
    const result = await prisma.$transaction(async (tx) => {
      let record = await tx.userSignInRecord.findUnique({
        where: { userId }
      });

      const now = new Date();
      const todayZero = getTodayZero().getTime();
      const yesterdayZero = getYesterdayZero().getTime();

      if (!record) {
        record = await tx.userSignInRecord.create({
          data: {
            userId: userId,
            lastSignInDate: new Date(0), // 初始给个很早的时间
            continuousDays: 0
          }
        });
      }

      const lastSignInTime = record.lastSignInDate?.getTime() ?? 0;

      if (lastSignInTime >= todayZero) {
        throw new Error('ALREADY_SIGNED_IN_TODAY');
      }

      let newStreak = record.continuousDays;
      if (lastSignInTime >= yesterdayZero && lastSignInTime < todayZero) {
        // 连续签到
        newStreak += 1;
      } else {
        // 断签或者第一次签到
        newStreak = 1;
      }

      // 如果超过 7 天，重置为 1 (根据需求：最大值为 7)
      if (newStreak > 7) {
        newStreak = 1;
      }

      // 奖励计算逻辑 (例如：1天1币，7天可以给大奖比如20币)
      let rewardCoins = 0;
      if (newStreak === 7) {
        rewardCoins = 20;
      } else {
        rewardCoins = newStreak; // 1, 2, 3, 4, 5, 6
      }

      // 更新记录
      await tx.userSignInRecord.update({
        where: { userId },
        data: {
          continuousDays: newStreak,
          lastSignInDate: now,
          totalSignInDays: (record?.totalSignInDays ?? 0) + 1
        }
      });

      return { newStreak, rewardCoins };
    });

    // 调用钱包服务发放奖励
    let finalBalance = undefined;
    try {
      const rewardResult = await walletService.rewardCoins(userId, result.rewardCoins, 'SIGN_IN_REWARD', `SIGNIN_${result.newStreak}`);
      finalBalance = rewardResult.balanceAfter;
    } catch (err) {
      console.error('Reward coins failed after signin:', err);
    }

    res.json({
      success: true,
      reward_coins: result.rewardCoins,
      current_continuous_days: result.newStreak,
      balance_after: finalBalance
    });

  } catch (error: any) {
    if (error.message === 'ALREADY_SIGNED_IN_TODAY') {
      return res.status(400).json({ error: 'You have already signed in today.' });
    }
    console.error('Execute signin error:', error);
    res.status(500).json({ error: 'Failed to execute signin' });
  }
});

export default router;
