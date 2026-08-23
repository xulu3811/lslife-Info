import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    
    let result = await ssh.execCommand('docker ps', { cwd: '/home/lslife/backend' });
    console.log('Docker ps:', result.stdout);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
