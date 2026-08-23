import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    
    let result = await ssh.execCommand('find / -name npm -type f -executable 2>/dev/null');
    console.log('NPM paths:', result.stdout);
    
    result = await ssh.execCommand('find / -name pm2 -type f -executable 2>/dev/null');
    console.log('PM2 paths:', result.stdout);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
