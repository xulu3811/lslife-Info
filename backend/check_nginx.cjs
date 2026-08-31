const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  
  const res = await ssh.execCommand('ls -l /etc/nginx/conf.d/');
  console.log("conf.d:", res.stdout);
  
  const res2 = await ssh.execCommand('ls -l /etc/nginx/sites-available/');
  console.log("sites-available:", res2.stdout);
  
  const res3 = await ssh.execCommand('cat /etc/nginx/sites-available/default || cat /etc/nginx/nginx.conf');
  console.log("nginx config:", res3.stdout);
  
  ssh.dispose();
}
main();
