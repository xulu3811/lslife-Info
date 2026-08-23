import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Fetching PM2 logs...');
  const res = await ssh.execCommand('su - lslife -c "pm2 logs lslife-api --lines 100 --nostream"');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
