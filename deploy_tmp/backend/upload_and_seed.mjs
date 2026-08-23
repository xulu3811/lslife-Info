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

  console.log('1. Uploading icons to remote server...');
  await ssh.putDirectory(path.join(__dirname, 'public/assets/icons'), `${targetDir}/public/assets/icons`, {
    recursive: true,
    concurrency: 10,
  });

  console.log('2. Uploading seed.ts to remote server...');
  await ssh.putFile(path.join(__dirname, 'prisma/seed.ts'), `${targetDir}/prisma/seed.ts`);

  console.log('3. Re-seeding database and restarting server...');
  const result = await ssh.execCommand(
    [
      `cd ${targetDir}`,
      'export PATH=$HOME/.local/nodejs/bin:$PATH',
      'npm run seed',
      'pm2 restart all'
    ].join(' && ')
  );

  console.log('STDOUT:', result.stdout);
  if (result.stderr) console.error('STDERR:', result.stderr);
  
  if (result.code !== 0) {
    throw new Error(`Remote command failed with code ${result.code}`);
  }

  console.log('✅ Remote DB successfully seeded and server restarted! Icons are now live.');
}

main()
  .catch((err) => {
    console.error('Error:', err);
    process.exitCode = 1;
  })
  .finally(() => ssh.dispose());
