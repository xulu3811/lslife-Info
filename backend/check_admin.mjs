import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node -e \\"import { PrismaClient } from \'@prisma/client\'; const prisma = new PrismaClient(); prisma.adminUser.findMany().then(console.log).finally(() => prisma.\\\\\\());\\""');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main();
