import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
async function main() {
  const users = await prisma.user.findMany({ where: { realNameStatus: 'pending' } });
  console.log("Pending users count:", users.length);
  const admin = await prisma.user.findUnique({ where: { phone: '13828577665' } });
  console.log("Admin exists?", !!admin);
}
main().catch(console.error).finally(() => prisma.$disconnect());
