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
    
    console.log("Executing remote deployment commands for /home/lslife/backend...");
    const result = await ssh.execCommand(`
      su - lslife -c "
        echo 'Deploying backend to correct directory...'
        cp -r /home/lslife/release_tmp/backend/* /home/lslife/backend/
        cd /home/lslife/backend
        npm install --production
        npx prisma generate
        npx prisma db push --accept-data-loss
        pm2 restart all
      "
    `);
    console.log(result.stdout);
    if (result.stderr) console.error(result.stderr);
    
    console.log("✅ Remote deployment to CORRECT directory finished!");
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
