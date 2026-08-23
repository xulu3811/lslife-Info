import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const categories = await prisma.category.findMany({
    where: { parentId: null },
    select: { name: true, icon: true }
  });
  console.log("TOP-LEVEL CATEGORIES:");
  console.log(JSON.stringify(categories, null, 2));
}

main()
  .catch(e => console.error(e))
  .finally(() => prisma.$disconnect());
