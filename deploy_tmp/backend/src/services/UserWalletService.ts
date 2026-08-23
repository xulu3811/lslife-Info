import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

export class UserWalletService {
  /**
   * 扣减猫币核心逻辑（带乐观锁防并发控制）
   * @param userId 用户 ID
   * @param amount 扣除的猫币数量 (必须大于0)
   * @param tradeType 交易类型 (如 'CONSUME_POST')
   * @param relatedBizId 关联的业务ID (如 Post ID)
   */
  async consumeCoins(userId: string, amount: number, tradeType: string, relatedBizId?: string) {
    if (amount <= 0) throw new Error('Amount must be strictly positive');

    // 开启 Prisma 事务
    return await prisma.$transaction(async (tx) => {
      // 1. 查询钱包当前状态（包含 version）
      const wallet = await tx.userWallet.findUnique({
        where: { userId }
      });

      if (!wallet) {
        throw new Error('WALLET_NOT_FOUND');
      }

      // 2. 验证余额是否充足
      if (wallet.coinBalance < amount) {
        throw new Error('INSUFFICIENT_BALANCE');
      }

      // 3. 执行乐观锁扣费 Update
      const updateResult = await tx.userWallet.updateMany({
        where: {
          userId: userId,
          version: wallet.version // 【核心】乐观锁检查：必须与刚才查询的版本一致
        },
        data: {
          coinBalance: wallet.coinBalance - amount,
          version: wallet.version + 1 // 版本号自增
        }
      });

      // 4. 如果更新行数为 0，说明被其他并发请求抢先修改了余额
      if (updateResult.count === 0) {
        throw new Error('CONCURRENT_CONFLICT'); // 提示并发冲突，可重试
      }

      const balanceAfter = wallet.coinBalance - amount;

      // 5. 记入双账本流水 (WalletLogs)
      await tx.walletLog.create({
        data: {
          userId,
          amount: -amount, // 支出记录为负
          balanceAfter: balanceAfter,
          tradeType,
          relatedBizId
        }
      });

      return { success: true, balanceAfter };
    });
  }

  /**
   * 增加猫币核心逻辑（带乐观锁防并发控制）
   * @param userId 用户 ID
   * @param amount 增加的猫币数量 (必须大于0)
   * @param tradeType 交易类型 (如 'SIGN_IN_REWARD')
   * @param relatedBizId 关联的业务ID
   */
  async rewardCoins(userId: string, amount: number, tradeType: string, relatedBizId?: string) {
    if (amount <= 0) throw new Error('Amount must be strictly positive');

    return await prisma.$transaction(async (tx) => {
      let wallet = await tx.userWallet.findUnique({
        where: { userId }
      });

      // 如果钱包不存在，可能需要初始化
      if (!wallet) {
        wallet = await tx.userWallet.create({
          data: {
            userId: userId,
            coinBalance: 0,
            version: 0
          }
        });
      }

      const updateResult = await tx.userWallet.updateMany({
        where: {
          userId: userId,
          version: wallet.version
        },
        data: {
          coinBalance: wallet.coinBalance + amount,
          version: wallet.version + 1
        }
      });

      if (updateResult.count === 0) {
        throw new Error('CONCURRENT_CONFLICT');
      }

      const balanceAfter = wallet.coinBalance + amount;

      await tx.walletLog.create({
        data: {
          userId,
          amount: amount,
          balanceAfter: balanceAfter,
          tradeType,
          relatedBizId
        }
      });

      return { success: true, balanceAfter };
    });
  }
}
