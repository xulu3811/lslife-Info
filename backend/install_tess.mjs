import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  const res = await ssh.execCommand("su - lslife -c 'cd /home/lslife/lslife-backend && npm install tesseract.js'");
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
