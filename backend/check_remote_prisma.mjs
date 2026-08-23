import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();
const host = '115.191.6.95';
const username = 'root';
const password = 'Maxence2468;';

async function main() {
  await ssh.connect({ host, username, password });
  const result = await ssh.execCommand('ls -la /home/lslife/lslife-backend/prisma');
  console.log('PRISMA DIR:', result.stdout);
}
main().then(() => ssh.dispose());
