import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  const passwords = ['Maxence2468', 'Maxence2468;'];
  
  for (const pwd of passwords) {
    try {
      console.log(`Trying password: ${pwd}`);
      await ssh.connect({
        host: '115.191.6.95',
        username: 'root',
        password: pwd,
        readyTimeout: 10000
      });
      console.log(`SSH connected successfully with ${pwd}!`);
      
      let result = await ssh.execCommand('pwd; ls -la');
      console.log('Result:', result.stdout);
      
      result = await ssh.execCommand('find /root /home /var/www -maxdepth 3 -name "backend" -type d');
      console.log('Search backend:', result.stdout);
      
      ssh.dispose();
      return;
    } catch (err) {
      console.error(`Failed with ${pwd}:`, err.message);
    }
  }
}

main();
