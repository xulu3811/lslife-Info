import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

/**
 * 守护进程执行：同城动态冷热分级清理 (Dynamic Posts Cleanup)
 * 1. 软隔离 (Soft Delete): 超过 expiresAt 的记录状态变为 EXPIRED
 * 2. 物理销毁 (Hard Delete): 超过 90 天且处于 EXPIRED 状态，并且没有 evidenceHash 的记录被彻底删除
 */
async function cleanupDynamics() {
  const now = new Date();
  
  try {
    console.log(`[${now.toISOString()}] Starting DynamicPost cleanup...`);

    // 1. Soft Delete: AI_REVIEWING/MANUAL_REVIEWING/PUBLISHED -> EXPIRED
    const softDeleteResult = await prisma.dynamicPost.updateMany({
      where: {
        expiresAt: { lt: now },
        status: { notIn: ['EXPIRED', 'DELETED'] }
      },
      data: {
        status: 'EXPIRED'
      }
    });

    console.log(`[Soft Delete] Marked ${softDeleteResult.count} posts as EXPIRED.`);

    // 2. Hard Delete: > 90 days since createdAt AND status = EXPIRED AND evidenceHash is null
    const threshold90Days = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);

    const hardDeleteResult = await prisma.dynamicPost.deleteMany({
      where: {
        status: 'EXPIRED',
        createdAt: { lt: threshold90Days },
        // 只保留有存证的记录用于归档/纠纷回溯
        evidenceHash: null
      }
    });

    console.log(`[Hard Delete] Physically deleted ${hardDeleteResult.count} EXPIRED posts older than 90 days.`);

  } catch (error) {
    console.error(`[Error] Failed during DynamicPost cleanup:`, error);
  } finally {
    await prisma.$disconnect();
  }
}

// Check if run directly
if (import.meta.url === `file://${process.argv[1]}` || require.main === module) {
  cleanupDynamics();
}

export { cleanupDynamics };
