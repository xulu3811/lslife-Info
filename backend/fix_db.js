import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
async function run() {
  const r1 = await prisma.post.updateMany({ where: { status: 'PUBLISHED' }, data: { status: 'MANUAL_REVIEWING' } });
  const r2 = await prisma.post.updateMany({ where: { status: 'published' }, data: { status: 'MANUAL_REVIEWING' } });
  console.log('Updated:', r1.count + r2.count);
}
run().finally(() => prisma.$disconnect());
