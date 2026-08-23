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
  const users = await prisma.user.findMany({
    select: { id: true, nickname: true, avatar: true }
  });
  const nullNicknames = users.filter(u => u.nickname === null || u.nickname === undefined);
  console.log("Users with null nickname:", nullNicknames.length);
}
test().finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/lslife-backend/test_null_nicknames.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node test_null_nicknames.cjs"');
  
  console.log('STDOUT:', res.stdout);
  console.error('STDERR:', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
