const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
async function main() {
  const admin = await prisma.user.findUnique({ where: { phone: '13828577665' } });
  console.log(admin);
}
main().finally(() => prisma.$disconnect());
