import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  const res = await ssh.execCommand('su - lslife -c "pm2 logs --lines 150 --nostream"');
  
  console.log('STDOUT:', res.stdout);
  console.error('STDERR:', res.stderr);

  ssh.dispose();
}
main().catch(console.error);
