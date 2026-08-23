const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Killing specific PID...');
  await ssh.execCommand('kill -9 2520112');
  
  console.log('Checking processes...');
  const psRes = await ssh.execCommand('ps aux | grep node');
  console.log(psRes.stdout);
  
  ssh.dispose();
}
main();
