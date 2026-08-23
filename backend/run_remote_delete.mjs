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
    await ssh.putFile('d:/LsLife/backend/delete_test_users.mjs', '/home/lslife/lslife-backend/delete_test_users.mjs');
    
    console.log("Executing script on remote server...");
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd /home/lslife/lslife-backend
        node delete_test_users.mjs
      "
    `);
    
    console.log(result.stdout);
    if (result.stderr) console.error(result.stderr);
    
    console.log("✅ Remote deletion finished!");
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
