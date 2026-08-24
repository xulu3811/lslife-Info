import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected to server.');

    const targetDir = '/home/lslife/lslife-backend';
    
    await ssh.putFile('d:/GitHub-lslife-V6.0/backend/fix_db.mjs', `${targetDir}/fix_db.mjs`);
    console.log('Uploaded fix_db.mjs');
    
    const res = await ssh.execCommand(`su - lslife -c "cd ${targetDir} && node fix_db.mjs"`);
    console.log('Result:', res.stdout);
    if (res.stderr) console.error('Error:', res.stderr);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
}

main();
