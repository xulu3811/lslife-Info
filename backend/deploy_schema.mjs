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

    // Upload schema.prisma
    console.log('Uploading schema.prisma...');
    await ssh.putFile(
      path.resolve('d:/GitHub-lslife-V6.0/backend/prisma/schema.prisma'),
      '/home/lslife/backend/prisma/schema.prisma'
    );

    // Upload publish.ts
    console.log('Uploading publish.ts...');
    await ssh.putFile(
      path.resolve('d:/GitHub-lslife-V6.0/backend/src/modules/publish.ts'),
      '/home/lslife/backend/src/modules/publish.ts'
    );
    
    // Fix permissions
    await ssh.execCommand('chown -R lslife:lslife /home/lslife/backend/prisma/schema.prisma /home/lslife/backend/src/modules/publish.ts');

    // Run prisma commands and pm2 restart
    console.log('Running prisma and pm2 commands...');
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd /home/lslife/backend
        npx prisma generate
        npx prisma db push --accept-data-loss
        pm2 restart lslife-api
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
