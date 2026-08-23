import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  const res = await ssh.execCommand('su - lslife -c "pm2 show lslife-api"');
  console.log('PM2 SHOW:\n', res.stdout);

  ssh.dispose();
}
main().catch(console.error);
