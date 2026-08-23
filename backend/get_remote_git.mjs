import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();
const host = '115.191.6.95';
const username = 'root';
const password = 'Maxence2468;';

async function main() {
  await ssh.connect({ host, username, password });
  const result = await ssh.execCommand('cd /home/lslife/lslife-backend && git status && git log -1');
  console.log(result.stdout || result.stderr);
  
  const result2 = await ssh.execCommand('cd /home/lslife/lslife-backend && git diff prisma/schema.prisma');
  console.log('DIFF:', result2.stdout);
}
main().then(() => ssh.dispose());
