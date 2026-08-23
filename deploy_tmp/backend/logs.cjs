const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({host: '115.191.6.95', username: 'root', password: 'Maxence2468;'});
  const res = await ssh.execCommand('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:$PATH; pm2 logs lslife-api --lines 20 --nostream"');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main();
