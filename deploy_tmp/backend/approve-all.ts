import { prisma } from './src/lib/prisma.js';

async function main() {
  const res = await prisma.post.updateMany({
    where: { status: 'pending_review' },
    data: { status: 'published' }
  });
  console.log(`Approved ${res.count} posts.`);
}

main().catch(console.error).finally(() => prisma.$disconnect());
