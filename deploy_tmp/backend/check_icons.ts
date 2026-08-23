import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const categories = await prisma.category.findMany({
    where: {
      OR: [
        { name: { contains: '租房' } },
        { name: { contains: '租' } }
      ]
    }
  });
  console.log("Categories with 租:");
  categories.forEach(c => console.log(`${c.name} - ${c.iconUrl}`));
  
  const parent = await prisma.category.findFirst({
    where: { name: '租房' }
  });
  if (parent) {
      const children = await prisma.category.findMany({
        where: { parentId: parent.id }
      });
      console.log("\nChildren of 租房:");
      children.forEach(c => console.log(`${c.name} - ${c.iconUrl}`));
  }
}

main().catch(console.error).finally(() => prisma.$disconnect());
