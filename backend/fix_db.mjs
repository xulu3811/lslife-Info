import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const res = await prisma.post.updateMany({
    where: {
      category: 'sys_dynamic',
      postType: 'CLASSIFIED'
    },
    data: {
      postType: 'MOMENT'
    }
  });
  console.log(`Updated ${res.count} records.`);
  await prisma.$disconnect();
}

main().catch(console.error);
