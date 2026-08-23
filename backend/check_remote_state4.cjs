const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('\n--- PM2 LOGS ---');
  const logRes = await ssh.execCommand('cat /home/lslife/.pm2/logs/lslife-api-error.log | tail -n 50');
  console.log(logRes.stdout);

  ssh.dispose();
}
main();
