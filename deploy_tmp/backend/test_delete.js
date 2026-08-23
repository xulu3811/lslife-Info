import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  
  console.log('Stopping PM2 and killing zombie node processes...');
  await ssh.execCommand('export PATH=$PATH:/home/lslife/.local/nodejs/bin; pm2 stop all');
  await ssh.execCommand('pkill -f node || true');
  await ssh.execCommand('fuser -k 4000/tcp || true');

  console.log('Starting PM2 lslife-api...');
  const restartRes = await ssh.execCommand('export PATH=$PATH:/home/lslife/.local/nodejs/bin; pm2 start /home/lslife/lslife-backend/ecosystem.config.cjs', { cwd: '/home/lslife/lslife-backend' });
  console.log('PM2 Start Output:\n', restartRes.stdout || restartRes.stderr);

  console.log('Checking Node port 4000 owner...');
  const ssRes = await ssh.execCommand('ss -tulpn | grep 4000');
  console.log(ssRes.stdout);

  console.log('Testing DELETE /api/chat/sessions/test directly on localhost...');
  const testRes = await ssh.execCommand('curl -i -X DELETE http://127.0.0.1:4000/api/chat/sessions/test');
  console.log('Result:\n', testRes.stdout);

  ssh.dispose();
}

run();
