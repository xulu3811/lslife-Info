import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
async function main() {
  const users = await prisma.user.findMany({ where: { email: 'xl441825@126.com' } });
  console.log('Users with xl441825@126.com:', users.map(u => u.phone));
  await prisma.$disconnect();
}
main().catch(console.error);
