import { NodeSSH } from 'node-ssh';
import path from 'path';

const ssh = new NodeSSH();

async function main() {
  console.log('Connecting to server 115.191.6.95...');
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('Connected.');

  const targetDir = '/home/lslife/lslife-backend';

  console.log(`Uploading dist to ${targetDir}/dist...`);
  await ssh.putDirectory(
    path.resolve('d:/GitHub-lslife-V6.0/backend/dist'),
    `${targetDir}/dist`,
    {
      recursive: true,
      concurrency: 10,
      validate: (itemPath) => !itemPath.includes('node_modules')
    }
  );

  console.log(`Uploading src to ${targetDir}/src...`);
  await ssh.putDirectory(
    path.resolve('d:/GitHub-lslife-V6.0/backend/src'),
    `${targetDir}/src`,
    {
      recursive: true,
      concurrency: 10,
      validate: (itemPath) => !itemPath.includes('node_modules')
    }
  );

  console.log(`Uploading prisma to ${targetDir}/prisma...`);
  await ssh.putDirectory(
    path.resolve('d:/GitHub-lslife-V6.0/backend/prisma'),
    `${targetDir}/prisma`,
    {
      recursive: true,
      concurrency: 10,
      validate: (itemPath) => !itemPath.includes('node_modules') && !itemPath.includes('.db')
    }
  );

  console.log('Fixing permissions...');
  await ssh.execCommand(`chown -R lslife:lslife ${targetDir}`);

  console.log('Killing all root node processes to release port 4000...');
  await ssh.execCommand('killall node');

  console.log('Generating Prisma Client in the target directory...');
  const prismaRes = await ssh.execCommand(`su - lslife -c "cd ${targetDir} && npx prisma generate"`);
  console.log(prismaRes.stdout);
  console.log(prismaRes.stderr);

  console.log('Restarting PM2 lslife-api with --update-env...');
  const pm2Res = await ssh.execCommand(`su - lslife -c "pm2 restart lslife-api --update-env"`);
  console.log(pm2Res.stdout);
  console.log(pm2Res.stderr);

  ssh.dispose();
  console.log('Deployment complete!');
}

main().catch(err => {
  console.error('Deployment error:', err);
  process.exit(1);
});
