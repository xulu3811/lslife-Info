import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    
    console.log("Rebuilding backend and restarting pm2 as lslife user...");
    const buildResult = await ssh.execCommand(
      'su - lslife -c "cd /home/lslife/lslife-backend && npm run build && pm2 restart all"'
    );
    console.log("STDOUT:", buildResult.stdout);
    if (buildResult.stderr) console.error("STDERR:", buildResult.stderr);

    console.log("✅ Repair complete!");
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
