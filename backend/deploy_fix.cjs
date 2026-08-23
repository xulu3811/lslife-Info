const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  await ssh.putFile('src/modules/publish.ts', '/home/lslife/backend/src/modules/publish.ts');
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/backend && export PATH=/home/lslife/.local/nodejs/bin:$PATH && npm run build && pm2 restart lslife-api"');
  console.log("STDOUT:", res.stdout);
  console.log("STDERR:", res.stderr);
  ssh.dispose();
}
run();
