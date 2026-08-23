import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    console.log("Connecting to server...");
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    
    console.log("Uploading script...");
    await ssh.putFile('d:/LsLife/backend/seed_remote_users.mjs', '/home/lslife/lslife-backend/seed_remote_users.mjs');
    
    console.log("Executing script on remote server...");
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd /home/lslife/lslife-backend
        node seed_remote_users.mjs
      "
    `);
    
    console.log(result.stdout);
    if (result.stderr) console.error(result.stderr);
    
    console.log("✅ Remote seed finished!");
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
