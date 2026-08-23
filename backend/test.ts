import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
prisma.category.findMany({where:{parentId:null}, orderBy: {sortOrder: 'asc'}}).then(res => {
    console.log(res.map(c => c.name));
}).finally(() => prisma.$disconnect());
