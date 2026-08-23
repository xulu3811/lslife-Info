import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  const res = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "SELECT phone, \\"realNameStatus\\" FROM \\"User\\" WHERE phone = \'13828571815\';"');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
