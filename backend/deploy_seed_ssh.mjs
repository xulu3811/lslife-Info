import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    
    console.log('Uploading script...');
    await ssh.putFile('backend/scripts/seed_test_accounts.ts', '/var/www/lslife-backend/scripts/seed_test_accounts.ts');
    
    console.log('Executing script...');
    const result = await ssh.execCommand(`source ~/.nvm/nvm.sh && npx tsx scripts/seed_test_accounts.ts`, { cwd: '/var/www/lslife-backend' });
    console.log('STDOUT:', result.stdout);
    if (result.stderr) console.error('STDERR:', result.stderr);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
