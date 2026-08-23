import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  console.log("Uploading auth.ts to server...");
  await ssh.putFile('d:/GitHub-lslife-V6.0/backend/src/middleware/auth.ts', '/home/lslife/lslife-backend/src/middleware/auth.ts');

  console.log("Rebuilding backend and restarting pm2 as lslife user...");
  const buildResult = await ssh.execCommand(
    'su - lslife -c "cd /home/lslife/lslife-backend && npm run build; pm2 restart all"'
  );
  console.log("STDOUT:", buildResult.stdout);
  if (buildResult.stderr) console.error("STDERR:", buildResult.stderr);

  ssh.dispose();
}
main();
