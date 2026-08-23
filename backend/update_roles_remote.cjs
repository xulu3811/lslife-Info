const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  
  const script = `
    const { PrismaClient } = require('@prisma/client');
    const prisma = new PrismaClient();
    async function r() {
      // 1. 获取用户
      const user1 = await prisma.user.findUnique({ where: { phone: '19926387658' } });
      const user2 = await prisma.user.findUnique({ where: { phone: '19007643665' } });
      
      if (user1) {
        // 将 user1 设为已认证商家 会员
        await prisma.user.update({
          where: { id: user1.id },
          data: {
            role: 'MERCHANT_VERIFIED',
            membershipTier: 'vip',
            realNameStatus: 'verified'
          }
        });
        
        // Upsert 认证表
        await prisma.merchantCertification.upsert({
          where: { userId: user1.id },
          update: { status: 'APPROVED' },
          create: {
            userId: user1.id,
            status: 'APPROVED',
            certType: 'ENTERPRISE',
            storeName: '官方认证商家',
            categoryId: 'cat_service',
            contactName: user1.nickname || '商家',
            contactPhone: user1.phone
          }
        });
        console.log("Updated user 19926387658 to Certified Merchant VIP.");
      } else {
        console.log("User 19926387658 not found!");
      }
      
      if (user2) {
        // 将 user2 设为未认证的普通用户
        await prisma.user.update({
          where: { id: user2.id },
          data: {
            role: 'USER_PERSONAL',
            membershipTier: 'free',
            realNameStatus: 'none'
          }
        });
        
        // 删除其商家认证（如果有的话）
        try {
          await prisma.merchantCertification.delete({
            where: { userId: user2.id }
          });
        } catch (e) {
          // ignore if not exists
        }
        console.log("Updated user 19007643665 to Uncertified Normal User.");
      } else {
        console.log("User 19007643665 not found!");
      }
    }
    r().catch(console.error).finally(() => prisma.$disconnect());
  `;
  
  await ssh.execCommand(`cat << 'EOF' > /home/lslife/backend/update_roles.cjs\n${script}\nEOF\n`);
  const res = await ssh.execCommand(`su - lslife -c "cd /home/lslife/backend && /home/lslife/.local/nodejs/bin/node update_roles.cjs"`);
  console.log("STDOUT:", res.stdout);
  console.log("STDERR:", res.stderr);
  ssh.dispose();
}
run();
