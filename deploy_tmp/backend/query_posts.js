import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

async function main() {
  const posts = await prisma.post.findMany({ take: 5, orderBy: { createdAt: 'desc' }});
  console.log(JSON.stringify(posts, null, 2));
}

main().catch(console.error).finally(() => prisma.$disconnect());
