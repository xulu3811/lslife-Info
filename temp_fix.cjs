
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
async function run() {
  await prisma.user.updateMany({
    where: { nickname: '奶龙' },
    data: { avatar: 'https://mentalhlp.site/uploads/b5ad4b29-221b-4da0-a8e1-25da96cddebf.jpg' }
  });
  await prisma.user.updateMany({
    where: { avatar: { contains: 'unsplash' } },
    data: { avatar: 'https://mentalhlp.site/uploads/5f331d0a-657e-4da8-8a05-f7794773542d.jpg' }
  });
  console.log('USER AVATARS UPDATED SUCCESSFULLY!');
  process.exit(0);
}
run();
