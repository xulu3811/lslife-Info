const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && npx prisma studio" &');
  // Just run a simple psql query instead of messing with prisma inside node
  const dbRes = await ssh.execCommand('su - lslife -c "psql -d lslife -c \\"SELECT username FROM \\\\\\"AdminUser\\\\\\" \\""');
  console.log("DB output:", dbRes.stdout);
  ssh.dispose();
}
main();
