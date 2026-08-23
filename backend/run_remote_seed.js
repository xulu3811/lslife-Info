import { NodeSSH } from 'node-ssh';

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
    
    console.log('Uploading local seed to remote server...');
    await ssh.putFile('d:\\LsLife\\backend\\prisma\\seed.ts', '/home/lslife/backend/prisma/seed.ts');
    console.log('Upload complete.');

    console.log('Executing seed on server...');
    const result = await ssh.execCommand('export PATH=/home/lslife/.local/nodejs/bin:$PATH && npx tsx prisma/seed.ts', { cwd: '/home/lslife/backend' });
    console.log('STDOUT:', result.stdout);
    console.log('STDERR:', result.stderr);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
