import { NodeSSH } from 'node-ssh';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ssh = new NodeSSH();
const privateKeyPath = path.join(process.env.USERPROFILE || process.env.HOME || '', '.ssh', 'id_ed25519');

async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'lslife', privateKeyPath });
  await ssh.putFile(path.join(__dirname, 'scripts/update-admin-password.ts'), '/home/lslife/lslife-backend/scripts/update-admin-password.ts');
  const res = await ssh.execCommand('export PATH=/home/lslife/.local/nodejs/bin:$PATH && cd /home/lslife/lslife-backend && npx tsx scripts/update-admin-password.ts "NtktiC726Kbmt3oMM8B5EgVQ"');
  console.log(res.stdout);
  console.error(res.stderr);
}
main().catch(console.error).finally(() => ssh.dispose());
