const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Checking parent PID of node...');
  const psRes = await ssh.execCommand('ps -ef | grep node');
  console.log(psRes.stdout);
  
  ssh.dispose();
}
main();
