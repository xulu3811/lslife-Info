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

async function test() {
  const userSy = await prisma.user.findFirst({
    where: { nickname: 'Sy' },
    include: { merchantCertification: true }
  });
  console.log("User Sy:", JSON.stringify(userSy, null, 2));

  const allCerts = await prisma.merchantCertification.findMany({});
  console.log("All merchant certs:", JSON.stringify(allCerts, null, 2));
}
test().finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/backend/check_sy.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/backend && node check_sy.cjs"');
  console.log('STDOUT:\n', res.stdout);
  console.error('STDERR:\n', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
