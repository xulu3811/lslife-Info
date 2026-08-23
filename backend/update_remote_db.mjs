import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

const host = '115.191.6.95';
const username = 'root';
const password = 'Maxence2468;';

async function main() {
  console.log(`Connecting ${username}@${host}...`);
  await ssh.connect({ host, username, password });

  const targetDir = '/home/lslife/lslife-backend';

  console.log('Running prisma db seed or direct SQL update...');
  const result = await ssh.execCommand(
    [
      `cd ${targetDir}`,
      'export PATH=/home/lslife/.local/nodejs/bin:/usr/local/bin:/usr/bin:$PATH',
      'npm run build',
      'npx prisma generate',
      'npx prisma db push',
    ].join(' && '),
  );
  
  console.log('STDOUT:', result.stdout);
  console.log('STDERR:', result.stderr);
  
  if (result.code !== 0) {
    console.error(`Failed with code ${result.code}`);
  } else {
    console.log('Update completed successfully.');
  }
}

main()
  .catch((err) => {
    console.error('Error:', err);
    process.exitCode = 1;
  })
  .finally(() => ssh.dispose());
