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

  const targetDir = '/home/lslife/lslife-backend';

  console.log('Uploading seed.ts...');
  await ssh.putFiles([
    { local: path.join(__dirname, 'prisma/seed.ts'), remote: `${targetDir}/prisma/seed.ts` },
  ]);

  console.log('Running npm run seed and restarting PM2...');
  const result = await ssh.execCommand(
    [
      `cd ${targetDir}`,
      'export PATH=$HOME/.local/nodejs/bin:$PATH',
      'npm run seed',
      'pm2 restart all',
      'pm2 save'
    ].join(' && '),
  );
  console.log(result.stdout || result.stderr);
  if (result.code !== 0) {
    throw new Error(`Remote command failed with code ${result.code}`);
  }

  console.log('Seed deployed and server restarted.');
}

main()
  .catch((err) => {
    console.error('Error:', err);
    process.exitCode = 1;
  })
  .finally(() => ssh.dispose());
