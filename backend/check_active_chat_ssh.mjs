import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  const res1 = await ssh.execCommand('cat /home/lslife/backend/dist/modules/chat.js');
  console.log('--- OLD CHAT.JS IN PM2 ---');
  console.log(res1.stdout);

  ssh.dispose();
}
main().catch(console.error);
