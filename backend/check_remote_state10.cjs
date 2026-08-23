const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Checking root PM2...');
  const sysRes = await ssh.execCommand('pm2 status');
  console.log(sysRes.stdout);
  
  ssh.dispose();
}
main();
