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
  console.log("=== USERS ===");
  const users = await prisma.user.findMany({
    select: { id: true, nickname: true, phone: true }
  });
  console.log(JSON.stringify(users, null, 2));

  console.log("=== CHAT SESSIONS ===");
  const sessions = await prisma.chatSession.findMany({});
  console.log(JSON.stringify(sessions, null, 2));

  console.log("=== RECENT CHAT MESSAGES ===");
  const msgs = await prisma.chatMessage.findMany({
    take: 10,
    orderBy: { createdAt: 'desc' }
  });
  console.log(JSON.stringify(msgs, null, 2));

  // Test what /sessions returns for each user
  for (const u of users) {
    const userSessions = await prisma.chatSession.findMany({
      where: {
        OR: [{ user1Id: u.id }, { user2Id: u.id }],
        NOT: { deletedBy: { has: u.id } }
      },
      orderBy: { updatedAt: 'desc' }
    });
    console.log(\`User \${u.nickname} (\${u.id}) has \${userSessions.length} sessions\`);
  }
}
test().finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/lslife-backend/check_im_state.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node check_im_state.cjs"');
  
  console.log('STDOUT:\n', res.stdout);
  console.error('STDERR:\n', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
