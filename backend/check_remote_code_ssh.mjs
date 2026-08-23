import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  const res1 = await ssh.execCommand('grep -n -C 5 "deletedBy" /home/lslife/lslife-backend/src/realtime/hub.ts');
  console.log('--- /src/realtime/hub.ts ---');
  console.log(res1.stdout);

  const res2 = await ssh.execCommand('grep -n -C 5 "deletedBy" /home/lslife/lslife-backend/dist/realtime/hub.js');
  console.log('--- /dist/realtime/hub.js ---');
  console.log(res2.stdout);

  const res3 = await ssh.execCommand('grep -n -C 5 "DELETE" /home/lslife/lslife-backend/src/modules/chat.ts');
  console.log('--- /src/modules/chat.ts ---');
  console.log(res3.stdout);

  ssh.dispose();
}
main().catch(console.error);
