import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    
    console.log("--- SYSTEM INFO ---");
    let res = await ssh.execCommand('uname -a && free -m && df -h');
    console.log(res.stdout);

    console.log("--- DOCKER CONTAINERS ---");
    res = await ssh.execCommand('docker ps -a');
    console.log(res.stdout);

    console.log("--- PM2 STATUS (lslife) ---");
    res = await ssh.execCommand('su - lslife -c "pm2 status"');
    console.log(res.stdout);

    console.log("--- POSTGRES DB ---");
    res = await ssh.execCommand('docker exec -i lslife-postgres psql -U lslife -d lslife -c "\\dt"');
    console.log(res.stdout);
    
    console.log("--- DELETE ONCE PASS ---");
    res = await ssh.execCommand('rm -f /home/lslife/.admin_pass_once');
    console.log(res.stdout);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
