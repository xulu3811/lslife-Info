import { NodeSSH } from 'node-ssh';
import path from 'path';

const ssh = new NodeSSH();

async function main() {
  try {
    console.log('Connecting to server 115.191.6.95...');
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected.');

    const targetDir = '/home/lslife/lslife-backend';

    console.log(`Uploading local dist to ${targetDir}/dist...`);
    // Delete remote dist first to ensure no stale files
    await ssh.execCommand(`rm -rf ${targetDir}/dist`);
    
    await ssh.putDirectory(
      path.resolve('d:/GitHub-lslife-V6.0/backend/dist'),
      `${targetDir}/dist`,
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

    console.log('Killing all node processes just in case port 4000 is blocked...');
    await ssh.execCommand('fuser -k 4000/tcp');

    console.log('Generating Prisma Client and DB Push...');
    const prismaRes = await ssh.execCommand(`su - lslife -c "cd ${targetDir} && npx prisma generate && npx prisma db push --accept-data-loss"`);
    console.log(prismaRes.stdout);
    if (prismaRes.stderr) console.error(prismaRes.stderr);

    console.log('Restarting PM2 lslife-api...');
    const pm2Res = await ssh.execCommand(`su - lslife -c "pm2 restart lslife-api --update-env"`);
    console.log(pm2Res.stdout);
    
    console.log('Deploying admin-web to /var/www/html/admin-web...');
    await ssh.execCommand('rm -rf /var/www/html/admin-web');
    await ssh.execCommand('mkdir -p /var/www/html/admin-web');
    await ssh.putDirectory(
      path.resolve('d:/GitHub-lslife-V6.0/admin-web/dist'),
      '/var/www/html/admin-web',
      {
        recursive: true,
        concurrency: 10
      }
    );
    console.log('Admin web deployment complete!');

    ssh.dispose();
    console.log('Deployment complete!');
  } catch (err) {
    console.error('Deployment error:', err);
    process.exit(1);
  }
}

main();
