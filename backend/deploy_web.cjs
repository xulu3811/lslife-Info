const { NodeSSH } = require('node-ssh');
const path = require('path');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  
  console.log('Deploying admin-web to /var/www/html/admin-web...');
  await ssh.execCommand('rm -rf /var/www/html/admin-web');
  await ssh.execCommand('mkdir -p /var/www/html/admin-web');
  await ssh.putDirectory(
    path.resolve('d:/GitHub-lslife-V6.0/admin-web/dist'),
    '/var/www/html/admin-web',
    { recursive: true, concurrency: 10 }
  );
  console.log('Admin web deployment complete!');
  ssh.dispose();
}
main();
