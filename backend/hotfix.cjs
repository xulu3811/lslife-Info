const { NodeSSH } = require('node-ssh');
const path = require('path');
const ssh = new NodeSSH();

async function run() {
  try {
    await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
    
    const cmds = [
      'cd /home/lslife/backend',
      'export PATH=/home/lslife/.nvm/versions/node/v20.18.1/bin:/home/lslife/.local/nodejs/bin:/usr/local/bin:/usr/bin:$PATH',
      'npm install',
      'npm run build',
      'pm2 restart lslife-api'
    ].join(' && ');

    const buildAndRestart = await ssh.execCommand(`su - lslife -c "${cmds}"`);
    console.log("Stdout:", buildAndRestart.stdout);
    console.log("Stderr:", buildAndRestart.stderr);

    ssh.dispose();
  } catch (e) {
    console.error(e);
  }
}
run();
