const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/backend && pm2 list"');
  console.log(res.stdout);
  ssh.dispose();
}
main();
