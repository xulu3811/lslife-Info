import { NodeSSH } from 'node-ssh';
import fs from 'fs';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected to server.');

    const targetDir = '/home/lslife/lslife-backend';
    const envPath = `${targetDir}/.env`;

    const result = await ssh.execCommand(`cat ${envPath}`);
    let currentEnv = result.stdout;
    
    if (!currentEnv.includes('AI_API_KEY')) {
      currentEnv += '\nAI_API_KEY="sk-30f79d21acbd487da71ec3cb5ce63d54"';
    } else {
      currentEnv = currentEnv.replace(/AI_API_KEY=.*/, 'AI_API_KEY="sk-30f79d21acbd487da71ec3cb5ce63d54"');
    }

    fs.writeFileSync('temp.env', currentEnv);
    await ssh.putFile('temp.env', envPath);
    
    console.log('Updated remote .env');
    
    const pm2Res = await ssh.execCommand(`su - lslife -c "pm2 restart lslife-api --update-env"`);
    console.log('PM2 restarted:', pm2Res.stdout);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
}

main();
