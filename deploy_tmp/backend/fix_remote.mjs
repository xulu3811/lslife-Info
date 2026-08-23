import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  const targetDir = '/home/lslife/lslife-backend';
  const result = await ssh.execCommand(
    [
      `cd ${targetDir}`,
      'export PATH=/home/lslife/.local/nodejs/bin:/usr/local/bin:/usr/bin:$PATH',
      'rm -rf src/services/deepseekReviewService.ts',
      'rm -rf dist',
      'npm run build',
      'pm2 restart lslife-api'
    ].join(' && ')
  );
  console.log(result.stdout);
  console.error(result.stderr);
  ssh.dispose();
}
main();
