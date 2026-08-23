const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function main() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    console.log("newUsers...");
    const newUsers = await prisma.user.count({ where: { createdAt: { gte: today } } });
    
    console.log("activeOrders...");
    const activeOrders = 0;

    const payments = { _sum: { amount: 0 } };

    console.log("pendingReviews...");
    const pendingReviews = await prisma.post.count({ 
      where: { status: { in: ['pending_review', 'MANUAL_REVIEWING'] } } 
    });
    
    console.log("pendingProfileReviews...");
    const pendingProfileReviews = await prisma.user.count({
      where: { profileReviewStatus: 'MANUAL_REVIEWING' }
    });
    
    console.log("pendingKyc...");
    const pendingKyc = await prisma.user.count({
      where: { realNameStatus: 'pending' }
    });
    
    console.log("pendingMerchantCerts...");
    const pendingMerchantCerts = await prisma.merchantCertification.count({
      where: { status: 'PENDING_REVIEW' }
    });

    console.log({
      newUsers,
      activeOrders,
      revenue: payments._sum.amount || 0,
      pendingReviews,
      pendingProfileReviews,
      pendingKyc,
      pendingMerchantCerts
    });
}
main().catch(console.error).finally(() => prisma.$disconnect());
