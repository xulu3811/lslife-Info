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
    
    console.log("Uploading hub.ts...");
    await ssh.putFile('D:/LsLife/backend/src/realtime/hub.ts', '/home/lslife/backend/src/realtime/hub.ts');
    
    console.log("Building and restarting PM2...");
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd /home/lslife/backend
        npm run build || npx tsc
        pm2 restart all
      "
    `);
    console.log(result.stdout);
    if (result.stderr) console.error(result.stderr);
    
    console.log("✅ Remote deployment finished!");
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
