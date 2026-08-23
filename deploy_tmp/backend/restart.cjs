const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({host: '115.191.6.95', username: 'root', password: 'Maxence2468;'});
  const res = await ssh.execCommand('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:$PATH; cd /home/lslife/backend; pm2 delete lslife-api; pm2 start ecosystem.config.cjs"');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main();
