const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function run() {
  try {
    await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
    const logs = await ssh.execCommand('su - lslife -c "pm2 logs --lines 50 --nostream"');
    console.log("STDOUT:", logs.stdout);
    console.log("STDERR:", logs.stderr);
    ssh.dispose();
  } catch (e) {
    console.error(e);
  }
}
run();
