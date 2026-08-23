import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  const res1 = await ssh.execCommand('ls -la /home/lslife/');
  console.log('LS /home/lslife:\n', res1.stdout);

  const res2 = await ssh.execCommand('grep -n -C 5 "deletedBy" /home/lslife/backend/dist/realtime/hub.js');
  console.log('GREP /home/lslife/backend/dist/realtime/hub.js:\n', res2.stdout);

  const res3 = await ssh.execCommand('grep -n -C 5 "DELETE" /home/lslife/backend/dist/modules/chat.js');
  console.log('GREP /home/lslife/backend/dist/modules/chat.js:\n', res3.stdout);

  ssh.dispose();
}
main().catch(console.error);
