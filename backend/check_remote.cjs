const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  const res = await ssh.execCommand('cat /home/lslife/backend/src/modules/publish.ts | grep -n -A 5 -B 5 "publisherId"');
  console.log(res.stdout);
  ssh.dispose();
}
run();
