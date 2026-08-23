const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  console.log('Killing PM2...');
  const killRes = await ssh.execCommand('su - lslife -c "pm2 kill"');
  console.log(killRes.stdout);
  
  console.log('Starting PM2...');
  const startRes = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && pm2 start dist/index.js --name lslife-api && pm2 save"');
  console.log(startRes.stdout);
  console.log(startRes.stderr);

  ssh.dispose();
}
main();
