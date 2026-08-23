const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({host: '115.191.6.95', username: 'root', password: 'Maxence2468;'});
  await ssh.putFile('d:\\LsLife\\backend\\temp_query.js', '/home/lslife/backend/temp_query.js');
  
  const res = await ssh.execCommand('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:$PATH; cd /home/lslife/backend && node temp_query.js"');
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main();
