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
    
    console.log("Uploading release.tgz...");
    await ssh.putFile('d:/LsLife/deploy_tmp/release.tgz', '/home/lslife/release.tgz');
    
    console.log("Executing remote deployment commands...");
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd /home/lslife
        rm -rf release_tmp
        mkdir release_tmp
        tar -xzf release.tgz -C release_tmp
        
        echo 'Deploying backend...'
        cp -r release_tmp/backend/* /home/lslife/lslife-backend/
        cd /home/lslife/lslife-backend
        npm install --production
        npx prisma generate
        npx prisma db push --accept-data-loss
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
