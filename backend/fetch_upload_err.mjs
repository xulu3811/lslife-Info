import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  const res = await ssh.execCommand("grep -i upload /home/lslife/.pm2/logs/lslife-api-error.log | tail -n 20");
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
