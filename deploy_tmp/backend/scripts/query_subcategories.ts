import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const parents = await prisma.category.findMany({
    where: { name: { in: ['招聘求职', '租车服务', '兼职零工'] } }
  });
  
  const parentIds = parents.map(p => p.id);
  const subCats = await prisma.category.findMany({
    where: { parentId: { in: parentIds } },
    select: { id: true, name: true, parentId: true }
  });
  console.log(subCats);
}

main().finally(() => prisma.$disconnect());
