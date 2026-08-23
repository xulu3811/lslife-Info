const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  // Tail pm2 logs to see recent requests
  const res = await ssh.execCommand('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:$PATH && pm2 logs lslife-api --lines 50 --nostream"');
  console.log(res.stdout);
  ssh.dispose();
}
run();
