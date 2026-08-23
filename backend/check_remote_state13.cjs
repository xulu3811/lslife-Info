const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Killing root PM2 daemon...');
  const pm2Res = await ssh.execCommand('pm2 kill');
  console.log(pm2Res.stdout);
  
  console.log('Force killing node...');
  await ssh.execCommand('killall -9 node');

  console.log('Restarting lslife PM2...');
  const restartRes = await ssh.execCommand('su - lslife -c "pm2 restart lslife-api --update-env"');
  console.log(restartRes.stdout);

  ssh.dispose();
}
main();
