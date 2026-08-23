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

    console.log('Uploading schema.prisma...');
    await ssh.putFile(
      path.resolve('d:/GitHub-lslife-V6.0/backend/prisma/schema.prisma'),
      `${targetDir}/prisma/schema.prisma`
    );

    console.log('Uploading admin.ts and publish.ts to src/modules...');
    await ssh.putFile(
      path.resolve('d:/GitHub-lslife-V6.0/backend/src/modules/admin.ts'),
      `${targetDir}/src/modules/admin.ts`
    );
    await ssh.putFile(
      path.resolve('d:/GitHub-lslife-V6.0/backend/src/modules/publish.ts'),
      `${targetDir}/src/modules/publish.ts`
    );

    // Also upload package.json just in case dependencies are missing
    await ssh.putFile(
      path.resolve('d:/GitHub-lslife-V6.0/backend/package.json'),
      `${targetDir}/package.json`
    );

    console.log('Fixing permissions...');
    await ssh.execCommand(`chown -R lslife:lslife ${targetDir}`);

    console.log('Killing zombie node process holding port 4000...');
    await ssh.execCommand('fuser -k 4000/tcp');

    console.log('Running prisma and pm2 commands...');
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd ${targetDir}
        npm install
        npx prisma generate
        npx prisma db push --accept-data-loss
        npm run build
        pm2 restart lslife-api --update-env
      "
    `);
    
    console.log('Output:', result.stdout);
    if (result.stderr) console.error('Error Output:', result.stderr);

    ssh.dispose();
    console.log('Deployment complete!');
  } catch (err) {
    console.error('Deployment error:', err);
    process.exit(1);
  }
}

main();
