import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
async function main() {
  const user = await prisma.user.findFirst({ where: { phone: '13828571818' } });
  console.log('User:', JSON.stringify(user, null, 2));
  
  if (user) {
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const used = await prisma.post.count({
      where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } },
    });
    console.log('used:', used);
    
    const MONTHLY_LIMIT = { free: 10, vip: 20, premium: 50 } as Record<string, number>;
    const limit = MONTHLY_LIMIT[user.membershipTier] ?? 10;
    const remainingFree = Math.max(0, limit - used);
    const availableQuota = remainingFree + user.paidQuota;
    console.log({limit, remainingFree, availableQuota, paidQuota: user.paidQuota});
  }
}
main().finally(() => prisma.$disconnect());
