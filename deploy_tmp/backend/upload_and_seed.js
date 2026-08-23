import { NodeSSH } from 'node-ssh';
import * as path from 'path';

const ssh = new NodeSSH();

async function main() {
  try {
    console.log('Connecting to SSH...');
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
      readyTimeout: 10000
    });
    console.log('SSH connected successfully!');
    
    const seedPathLocal = path.resolve('prisma/seed.ts');
    const schemaPathLocal = path.resolve('src/services/schema_engine.ts');
    
    console.log('Uploading seed.ts...');
    await ssh.putFile(seedPathLocal, '/home/lslife/lslife-backend/prisma/seed.ts');
    
    console.log('Uploading schema_engine.ts...');
    await ssh.putFile(schemaPathLocal, '/home/lslife/lslife-backend/src/services/schema_engine.ts');
    
    console.log('Executing seed command on server...');
    const result = await ssh.execCommand('export PATH=/home/lslife/.local/nodejs/bin:$PATH && npm run seed', { cwd: '/home/lslife/lslife-backend' });
    console.log('STDOUT:', result.stdout);
    console.log('STDERR:', result.stderr);
    
    console.log('Restarting PM2 to apply schema_engine changes (if applicable)...');
    const pm2Result = await ssh.execCommand('export PATH=/home/lslife/.local/nodejs/bin:$PATH && pm2 reload all', { cwd: '/home/lslife/lslife-backend' });
    console.log('PM2 Result STDOUT:', pm2Result.stdout);
    console.log('PM2 Result STDERR:', pm2Result.stderr);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
