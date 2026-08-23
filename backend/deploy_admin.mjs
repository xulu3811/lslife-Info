import { NodeSSH } from 'node-ssh';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ssh = new NodeSSH();

const host = process.env.SSH_HOST || '115.191.6.95';
const username = process.env.SSH_USER || 'lslife';
const privateKeyPath =
  process.env.SSH_PRIVATE_KEY || path.join(process.env.USERPROFILE || process.env.HOME || '', '.ssh', 'id_ed25519');

async function main() {
  console.log(`Connecting ${username}@${host} via key...`);
  await ssh.connect({
    host,
    username,
    privateKeyPath,
  });

  const targetDir = '/home/lslife/admin-web-dist';

  console.log(`Uploading admin-web dist to ${targetDir}...`);
  await ssh.execCommand(`mkdir -p ${targetDir}`);
  const status = await ssh.putDirectory(path.join(__dirname, '../admin-web/dist'), targetDir, {
    recursive: true,
    concurrency: 10,
  });

  if (!status) {
    throw new Error('Directory upload failed');
  }

  console.log('Admin-web deployed.');
}

main()
  .catch((err) => {
    console.error('Error:', err);
    process.exitCode = 1;
  })
  .finally(() => ssh.dispose());
