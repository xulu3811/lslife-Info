const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  const res = await ssh.execCommand('curl -s http://localhost:4000/api/posts/cmstvb9bk00025ba2omndx2qg');
  console.log("CRV Post:", res.stdout);
  ssh.dispose();
}
run();
