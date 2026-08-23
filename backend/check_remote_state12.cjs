const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Checking process 2520094...');
  const psRes = await ssh.execCommand('ps -f -p 2520094');
  console.log(psRes.stdout);
  
  ssh.dispose();
}
main();
