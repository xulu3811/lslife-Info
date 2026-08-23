const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('\n--- PM2 ERR LOGS ---');
  const errRes = await ssh.execCommand('cat /home/lslife/.pm2/logs/lslife-api-error-0.log | tail -n 50');
  console.log(errRes.stdout);

  console.log('\n--- PM2 OUT LOGS ---');
  const outRes = await ssh.execCommand('cat /home/lslife/.pm2/logs/lslife-api-out-0.log | tail -n 50');
  console.log(outRes.stdout);

  ssh.dispose();
}
main();
