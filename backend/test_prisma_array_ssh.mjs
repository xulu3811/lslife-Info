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
  const sessionId = 'cms4egvtp000410c4uu8pra0y';
  
  console.log("Before test, session:", await prisma.chatSession.findUnique({ where: { id: sessionId } }));

  // Test 1: deletedBy: []
  await prisma.chatSession.update({
    where: { id: sessionId },
    data: { deletedBy: [] }
  });
  console.log("After deletedBy: [], session:", await prisma.chatSession.findUnique({ where: { id: sessionId } }));

  // Test 2: deletedBy: { set: [] }
  await prisma.chatSession.update({
    where: { id: sessionId },
    data: { deletedBy: { set: [] } }
  });
  console.log("After deletedBy: { set: [] }, session:", await prisma.chatSession.findUnique({ where: { id: sessionId } }));
}
test().finally(() => prisma.$disconnect());
  `;

  await ssh.execCommand('cat > /home/lslife/lslife-backend/test_prisma_array.cjs', { stdin: script });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node test_prisma_array.cjs"');
  
  console.log('STDOUT:\n', res.stdout);
  console.error('STDERR:\n', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
