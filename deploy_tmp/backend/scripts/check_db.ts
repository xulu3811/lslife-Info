import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

async function main() {
  const merchants = await prisma.merchant.findMany({
    select: { id: true, name: true, ownerId: true }
  });
  console.log('--- MERCHANTS ---');
  console.table(merchants);

  const posts = await prisma.post.findMany({
    select: { id: true, title: true, category: true, userId: true }
  });
  console.log('--- POSTS ---');
  console.table(posts);

  const realUsers = await prisma.user.findMany({
    where: {
      NOT: [
        { nickname: { startsWith: '下单压测' } },
        { nickname: { startsWith: '财务测试' } },
        { nickname: { startsWith: '压测' } },
        { nickname: { startsWith: '测试' } }
      ]
    },
    select: { id: true, phone: true, nickname: true, createdAt: true }
  });
  console.log('--- NON-TEST USERS ---');
  console.table(realUsers);
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
