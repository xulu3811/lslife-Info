const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  const psRes = await ssh.execCommand('ps aux | grep node');
  console.log(psRes.stdout);
  ssh.dispose();
}
main();
