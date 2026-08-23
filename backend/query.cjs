const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function run() {
  const users = await prisma.user.findMany({ select: { id: true, nickname: true } });
  console.log(users);
  
  const posts = await prisma.post.findMany({ select: { id: true, title: true, userId: true } });
  console.log(posts);
}
run();
