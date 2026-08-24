const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

ssh.connect({
  host: '115.191.6.95',
  username: 'root',
  password: 'Maxence2468;'
}).then(() => {
  ssh.execCommand('rm -f /home/lslife/.admin_pass_once').then(function(result) {
    console.log('Success, STDOUT: ' + result.stdout);
    if(result.stderr) console.error('STDERR: ' + result.stderr);
    ssh.dispose();
  });
}).catch(err => {
    console.error("Connection failed:", err);
    process.exit(1);
});
