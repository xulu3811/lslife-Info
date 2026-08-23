const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('--- PM2 STATUS ---');
  const pm2Res = await ssh.execCommand('su - lslife -c "export PATH=$HOME/.local/nodejs/bin:$PATH && pm2 status"');
  console.log(pm2Res.stdout);

  console.log('\n--- PM2 LOGS ---');
  const logRes = await ssh.execCommand('cat /home/lslife/.pm2/logs/lslife-api-error.log | tail -n 50');
  console.log(logRes.stdout);

  console.log('\n--- DB TABLES ---');
  const dbRes = await ssh.execCommand('su - lslife -c "docker exec lslife-postgres psql -U lslife -d lslife -c \\"\\dt\\""');
  console.log(dbRes.stdout);
  console.log(dbRes.stderr);

  ssh.dispose();
}
main();
