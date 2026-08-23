const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
async function run() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });
  const res1 = await ssh.execCommand('curl -s http://localhost:4000/api/users/cmsogs407000x8s1srm2i8nyt/public');
  const res2 = await ssh.execCommand('curl -s http://localhost:4000/api/users/cms4egacu000310c4i1vryn7z/public');
  console.log("Nailong:", res1.stdout);
  console.log("Michael:", res2.stdout);
  ssh.dispose();
}
run();
