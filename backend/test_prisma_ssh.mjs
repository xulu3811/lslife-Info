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
  console.log("ALL SESSIONS:", allSessions.length);

  if (allSessions.length > 0) {
    const userId = allSessions[allSessions.length - 1].user1Id;
    const filtered = await prisma.chatSession.findMany({
      where: {
        OR: [{ user1Id: userId }, { user2Id: userId }],
        NOT: { deletedBy: { has: userId } }
      }
    });
    console.log("Filtered for user", userId, ":", filtered.length);
  }
}
test().finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/lslife-backend/test_query.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node test_query.cjs"');
  
  console.log('STDOUT:', res.stdout);
  console.error('STDERR:', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
