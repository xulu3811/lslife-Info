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
  const allSessions = await prisma.chatSession.findMany({});
  console.log("All Sessions with deletedBy:");
  allSessions.forEach(s => {
    console.log(s.id, s.user1Id, s.user2Id, s.deletedBy);
  });
}
test().finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/lslife-backend/test_deletedby.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node test_deletedby.cjs"');
  
  console.log('STDOUT:', res.stdout);
  console.error('STDERR:', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
