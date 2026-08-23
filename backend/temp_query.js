import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
prisma.post.findMany({ take: 10, orderBy: { createdAt: 'desc' }}).then(p => { 
  console.log(JSON.stringify(p, null, 2)); 
  prisma.$disconnect(); 
}).catch(e => console.error(e));
