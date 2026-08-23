import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
prisma.category.findMany({
      where: {
        OR: [
          { isActive: true },
          { isActive: { equals: undefined } }
        ]
      },
      orderBy: { sortOrder: 'asc' },
}).then(res => {
    const rootNodes = res.filter(c => c.parentId === null);
    console.log(rootNodes.map(c => ({ id: c.id, name: c.name, isActive: c.isActive })));
}).finally(() => prisma.$disconnect());
