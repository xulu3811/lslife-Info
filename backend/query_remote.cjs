const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  
  const script = `
    const { PrismaClient } = require('@prisma/client');
    const prisma = new PrismaClient();
    async function r() {
      const users = await prisma.user.findMany({ select: { id: true, nickname: true } });
      console.log("Users:", users);
      const posts = await prisma.post.findMany({ select: { id: true, title: true, userId: true }, take: 10 });
      console.log("Posts:", posts);
    }
    r().catch(console.error).finally(() => prisma.$disconnect());
  `;
  
  await ssh.execCommand(`cat << 'EOF' > /home/lslife/backend/q.cjs\n${script}\nEOF\n`);
  const res = await ssh.execCommand(`su - lslife -c "cd /home/lslife/backend && /home/lslife/.local/nodejs/bin/node q.cjs"`);
  console.log("STDOUT:", res.stdout);
  console.log("STDERR:", res.stderr);
  ssh.dispose();
}
run();
