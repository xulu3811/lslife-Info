const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('Killing rogue node processes...');
  await ssh.execCommand('killall node');

  console.log('\n--- DB TABLES ---');
  const dbRes = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "\\dt"');
  console.log(dbRes.stdout);
  
  // Restart pm2
  console.log('\nRestarting PM2...');
  const pm2Res = await ssh.execCommand('su - lslife -c "pm2 restart lslife-api --update-env"');
  console.log(pm2Res.stdout);

  ssh.dispose();
}
main();
