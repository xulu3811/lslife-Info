import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();
const host = '115.191.6.95';
const username = 'root';
const password = 'Maxence2468;';

async function main() {
  await ssh.connect({ host, username, password });
  const result = await ssh.execCommand(
    'cd /home/lslife/lslife-backend/src/modules && mv merchants.ts merchants.bak && cd /home/lslife/lslife-backend && npm run build'
  );
  console.log(result.stdout || result.stderr);
}
main().then(() => ssh.dispose());
