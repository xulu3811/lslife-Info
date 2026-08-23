import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();
const host = '115.191.6.95';
const username = 'root';
const password = 'Maxence2468;';

async function main() {
  await ssh.connect({ host, username, password });
  const result = await ssh.execCommand('cd /home/lslife/lslife-backend && export PATH=/home/lslife/.local/nodejs/bin:/usr/local/bin:/usr/bin:$PATH && npx prisma db pull');
  console.log(result.stdout || result.stderr);
  
  const content = await ssh.execCommand('cat /home/lslife/lslife-backend/prisma/schema.prisma');
  const fs = await import('fs');
  fs.writeFileSync('d:/LsLife/backend/prisma/schema.prisma', content.stdout);
  console.log('Schema pulled and saved locally.');
}
main().then(() => ssh.dispose());
