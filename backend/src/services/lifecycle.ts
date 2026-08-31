import { prisma } from '../lib/prisma.js';

export const startLifecycleCron = () => {
  const sweep = async () => {
    try {
      console.log('[LifecycleCron] Running lifecycle sweep...');
      const now = new Date();

      // 1. Archive expired MOMENT posts
      const archivedMoments = await prisma.post.updateMany({
        where: {
          postType: 'MOMENT',
          status: 'published',
          expireAt: { lt: now }
        },
        data: { status: 'archived' }
      });
      if (archivedMoments.count > 0) {
        console.log(`[LifecycleCron] Archived ${archivedMoments.count} MOMENT posts.`);
      }

      // 2. Expire expired CLASSIFIED posts
      const expiredPosts = await prisma.post.updateMany({
        where: {
          postType: 'CLASSIFIED',
          status: 'published',
          expireAt: { lt: now }
        },
        data: { status: 'expired' }
      });
      if (expiredPosts.count > 0) {
        console.log(`[LifecycleCron] Expired ${expiredPosts.count} CLASSIFIED posts.`);
      }

      // 3. Suspend expired merchants (90 days without confirmation or expired cert)
      const suspendedMerchants = await prisma.merchantCertification.updateMany({
        where: {
          status: 'APPROVED',
          OR: [
            { expireAt: { lt: now } },
            { lastConfirmedAt: { lt: new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000) } }
          ]
        },
        data: { status: 'SUSPENDED' }
      });
      if (suspendedMerchants.count > 0) {
        console.log(`[LifecycleCron] Suspended ${suspendedMerchants.count} merchant certifications.`);
      }

    } catch (error) {
      console.error('[LifecycleCron] Error during sweep:', error);
    }
  };

  // Run every hour to check for expirations
  setInterval(sweep, 1000 * 60 * 60);
  
  // Run once on startup after 5s
  setTimeout(sweep, 5000);
};
