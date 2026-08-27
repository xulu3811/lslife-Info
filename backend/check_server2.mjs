import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    
    const cmds = [
      'cat /home/lslife/lslife-backend/docker-compose.yml'
    ];

    for (const cmd of cmds) {
      console.log(`\n--- Running: ${cmd} ---`);
      const res = await ssh.execCommand(cmd);
      console.log(res.stdout);
      if (res.stderr) console.error(res.stderr);
    }
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err);
  }
}

main();
