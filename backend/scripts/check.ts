import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const categories = await prisma.category.findMany({
    where: { name: '数码 3C' }
  });
  console.log(categories);
}

main().finally(() => prisma.$disconnect());
