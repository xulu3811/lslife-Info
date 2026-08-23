import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  await ssh.putFile('check_email.mjs', '/home/lslife/lslife-backend/check_email.mjs');
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && node check_email.mjs"');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
