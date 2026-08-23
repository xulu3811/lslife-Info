import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  const script = `
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function fix() {
  const phones = ['13828577665', '19926387658'];
  for (const phone of phones) {
    const user = await prisma.user.findUnique({ where: { phone } });
    if (user) {
      await prisma.user.update({
        where: { id: user.id },
        data: { role: 'SUPERADMIN' }
      });
      console.log('Fixed admin role for:', phone);
    } else {
      console.log('User not found:', phone);
    }
  }
}

fix()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/lslife-backend/fix_db.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node fix_db.cjs"');
  console.log(res.stdout);
  console.log(res.stderr);

  ssh.dispose();
}

main().catch(console.error);
