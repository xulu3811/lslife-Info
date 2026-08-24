import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    console.log('Connecting to server 115.191.6.95...');
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected.');

    const res = await ssh.execCommand('rm -f /home/lslife/.admin_pass_once');
    console.log('Delete result:', res.stdout, res.stderr);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err);
  }
}

main();
