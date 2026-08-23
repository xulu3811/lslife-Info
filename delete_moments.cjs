const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
async function main() {
  const result = await prisma.post.deleteMany({
    where: { postType: 'MOMENT' }
  });
  console.log(`Deleted ${result.count} MOMENT posts.`);
}
main().catch(console.error).finally(() => prisma.$disconnect());
