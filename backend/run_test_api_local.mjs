import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  await ssh.putFile('d:/GitHub-lslife-V6.0/backend/test_api_local.cjs', '/home/lslife/lslife-backend/test_api_local.cjs');
  const res = await ssh.execCommand("su - lslife -c 'cd /home/lslife/lslife-backend && node test_api_local.cjs'");
  console.log(res.stdout);
  if (res.stderr) console.error(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
