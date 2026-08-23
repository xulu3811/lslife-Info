import { NodeSSH } from 'node-ssh';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ssh = new NodeSSH();

const host = process.env.SSH_HOST || '115.191.6.95';
const username = process.env.SSH_USER || 'lslife';
const privateKeyPath = process.env.SSH_PRIVATE_KEY || path.join(process.env.USERPROFILE || process.env.HOME || '', '.ssh', 'id_ed25519');

async function main() {
  console.log(`Connecting ${username}@${host} via key...`);
  await ssh.connect({
    host,
    username,
    privateKeyPath,
  });

  const targetDir = '/home/lslife/lslife-backend/public/assets/icons';

  console.log(`Uploading local public/assets/icons to ${targetDir}...`);
  const status = await ssh.putDirectory(path.join(__dirname, 'public/assets/icons'), targetDir, {
    recursive: true,
    concurrency: 10,
  });

  console.log('Upload status:', status ? 'Success' : 'Failed');
  ssh.dispose();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
