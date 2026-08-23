const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Checking systemd services...');
  const sysRes = await ssh.execCommand('systemctl list-units --type=service | grep lslife');
  console.log(sysRes.stdout);
  
  ssh.dispose();
}
main();
