import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    
    let result = await ssh.execCommand('cat ecosystem.config.cjs', { cwd: '/home/lslife/backend' });
    console.log('Ecosystem:', result.stdout);

    result = await ssh.execCommand('ls -la /root/backend || ls -la /home/lslife/backend/dist', { cwd: '/home/lslife/backend' });
    console.log('Dir:', result.stdout);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
