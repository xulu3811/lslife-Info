const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function run() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected to server');
    
    // Delete the file
    const result = await ssh.execCommand('rm -f /home/lslife/.admin_pass_once');
    console.log('Command output:', result.stdout);
    console.log('Command stderr:', result.stderr);
    console.log('File successfully deleted.');
  } catch (err) {
    console.error('Error:', err);
  } finally {
    ssh.dispose();
  }
}
run();
