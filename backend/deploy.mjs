import { NodeSSH } from 'node-ssh';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const ssh = new NodeSSH();

async function main() {
  try {
    console.log("Connecting...");
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    console.log("Connected!");
    
    const filesToUpload = [
      { local: path.join(__dirname, 'prisma/schema.prisma'), remote: '/home/lslife/backend/prisma/schema.prisma' },
      { local: path.join(__dirname, 'src/modules/ai.ts'), remote: '/home/lslife/backend/src/modules/ai.ts' },
      { local: path.join(__dirname, 'src/modules/auth.ts'), remote: '/home/lslife/backend/src/modules/auth.ts' },
      { local: path.join(__dirname, 'src/modules/billing.ts'), remote: '/home/lslife/backend/src/modules/billing.ts' },
      { local: path.join(__dirname, 'src/modules/publish.ts'), remote: '/home/lslife/backend/src/modules/publish.ts' },
    ];
    
    console.log("Uploading files...");
    await ssh.putFiles(filesToUpload);
    console.log("Upload completed!");
    
    console.log("Running commands...");
    const cmd = await ssh.execCommand(`su - lslife -c "cd /home/lslife/backend && npx prisma db push && npm run build && pm2 restart all"`);
    console.log("Output:\n", cmd.stdout, "\nERR:", cmd.stderr);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
