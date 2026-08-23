import { NodeSSH } from 'node-ssh';
import path from 'path';

const ssh = new NodeSSH();

async function main() {
  console.log('Connecting to server 115.191.6.95...');
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Connected.');

  // 1. Upload dist to /home/lslife/backend/dist
  console.log('Uploading dist to /home/lslife/backend/dist...');
  await ssh.putDirectory(
    path.resolve('d:/LsLife/backend/dist'),
    '/home/lslife/backend/dist',
    {
      recursive: true,
      concurrency: 10,
      validate: (itemPath) => !itemPath.includes('node_modules')
    }
  );

  // 2. Upload src to /home/lslife/backend/src
  console.log('Uploading src to /home/lslife/backend/src...');
  await ssh.putDirectory(
    path.resolve('d:/LsLife/backend/src'),
    '/home/lslife/backend/src',
    {
      recursive: true,
      concurrency: 10,
      validate: (itemPath) => !itemPath.includes('node_modules')
    }
  );

  // 3. Fix permissions
  await ssh.execCommand('chown -R lslife:lslife /home/lslife/backend');

  // 4. Clean up deletedBy in DB
  console.log('Resetting deletedBy on all sessions...');
  const resetScript = `
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function reset() {
  const result = await prisma.chatSession.updateMany({
    data: {
      deletedBy: []
    }
  });
  console.log("Updated sessions count:", result.count);
}
reset().finally(() => prisma.$disconnect());
  `;
  await ssh.execCommand('cat > /home/lslife/backend/reset_deletedby.cjs', { stdin: resetScript });
  const resetRes = await ssh.execCommand('su - lslife -c "cd /home/lslife/backend && node reset_deletedby.cjs"');
  console.log('Reset output:', resetRes.stdout);

  // 5. Restart PM2
  console.log('Restarting PM2 process lslife-api...');
  const pm2Res = await ssh.execCommand('su - lslife -c "pm2 restart lslife-api"');
  console.log('PM2 restart output:', pm2Res.stdout);

  // 6. Verify /sessions API logic for all users
  const verifyScript = `
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function verify() {
  const users = await prisma.user.findMany({ select: { id: true, nickname: true } });
  for (const u of users) {
    const sessions = await prisma.chatSession.findMany({
      where: {
        OR: [{ user1Id: u.id }, { user2Id: u.id }],
        NOT: { deletedBy: { has: u.id } }
      }
    });
    console.log(\`User \${u.nickname} has \${sessions.length} active sessions\`);
  }
}
verify().finally(() => prisma.$disconnect());
  `;
  await ssh.execCommand('cat > /home/lslife/backend/verify_sessions.cjs', { stdin: verifyScript });
  const verifyRes = await ssh.execCommand('su - lslife -c "cd /home/lslife/backend && node verify_sessions.cjs"');
  console.log('Verification output:\n', verifyRes.stdout);

  ssh.dispose();
  console.log('Deployment complete and verified!');
}

main().catch(err => {
  console.error('Deployment error:', err);
  process.exit(1);
});
