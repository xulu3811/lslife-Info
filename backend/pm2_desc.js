import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    
    let result = await ssh.execCommand('export PATH=/home/lslife/.local/nodejs/bin:$PATH && pm2 describe 0');
    console.log('Result:', result.stdout);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
