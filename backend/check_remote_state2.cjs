const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('--- NODE PROCESSES ---');
  const psRes = await ssh.execCommand('ps aux | grep node');
  console.log(psRes.stdout);

  console.log('\n--- SYSTEMD STATUS ---');
  const sysRes = await ssh.execCommand('systemctl status pm2-lslife');
  console.log(sysRes.stdout);
  
  console.log('\n--- DOCKER CONTAINERS ---');
  const dockerRes = await ssh.execCommand('docker ps -a');
  console.log(dockerRes.stdout);
  
  console.log('\n--- DB TABLES ---');
  const dbRes = await ssh.execCommand('docker exec lslife_postgres_1 psql -U lslife -d lslife -c "\\dt" || docker exec lslife-db-1 psql -U lslife -d lslife -c "\\dt" || docker exec postgres psql -U lslife -d lslife -c "\\dt"');
  console.log(dbRes.stdout);
  console.log(dbRes.stderr);

  ssh.dispose();
}
main();
