const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('\n--- Post Table Schema ---');
  const dbRes = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "\\d \\"Post\\""');
  console.log(dbRes.stdout);
  
  console.log('\n--- Enum types ---');
  const enumRes = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "\\dT"');
  console.log(enumRes.stdout);

  ssh.dispose();
}
main();
