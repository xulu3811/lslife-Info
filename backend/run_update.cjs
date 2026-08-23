const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  try {
    await ssh.connect({host: '115.191.6.95', username: 'root', password: 'Maxence2468;'});
    console.log('Running prisma generate, build, and pm2 restart as lslife...');
    const result = await ssh.execCommand(
      'su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:$PATH; cd /home/lslife/backend; npx prisma generate; npm run build; pm2 restart lslife-api"'
    );
    console.log('STDOUT:', result.stdout);
    console.log('STDERR:', result.stderr);
    ssh.dispose();
  } catch(e) {
    console.error(e);
  }
}
main();
