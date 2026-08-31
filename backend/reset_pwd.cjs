const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && npx tsx scripts/update-admin-password.ts \'LsLife2026\'"');
  console.log(res.stdout);
  console.log(res.stderr);
  
  // also let's just insert one if it doesn't exist
  const res2 = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && npx tsx scripts/create-admin.ts"');
  console.log(res2.stdout);
  
  ssh.dispose();
}
main();
