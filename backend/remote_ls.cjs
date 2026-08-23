const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({host: '115.191.6.95', username: 'root', password: 'Maxence2468;'});
  const res = await ssh.execCommand('ls -l /home/lslife/backend/uploads');
  console.log(res.stdout);
  ssh.dispose();
}
main();
