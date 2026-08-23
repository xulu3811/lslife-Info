const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  // Add CLASSIFIED to enum
  console.log('Adding CLASSIFIED to enum...');
  const addRes = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "ALTER TYPE \\"PostType\\" ADD VALUE IF NOT EXISTS \'CLASSIFIED\';"');
  console.log('Add enum:', addRes.stdout, addRes.stderr);

  // Update rows
  console.log('Updating rows...');
  const updateRes = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "UPDATE \\"Post\\" SET \\"postType\\" = \'CLASSIFIED\' WHERE \\"postType\\" = \'COMMERCE\';"');
  console.log('Update rows:', updateRes.stdout, updateRes.stderr);
  
  ssh.dispose();
}
main();
