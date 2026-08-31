const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  
  const res = await ssh.execCommand('cat /etc/nginx/sites-available/lslife');
  console.log("lslife config:", res.stdout);
  
  const res2 = await ssh.execCommand('ls -l /etc/nginx/sites-enabled/');
  console.log("sites-enabled:", res2.stdout);

  ssh.dispose();
}
main();
