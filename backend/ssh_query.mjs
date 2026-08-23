import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    
    console.log("--- BATCH FIX DELETEDBY ---");
    const result = await ssh.execCommand(`
      su - lslife -c "
        cd /home/lslife/backend
        cat << 'EOF' > fix_all.cjs
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
async function run() {
  try {
    const res = await prisma.chatSession.updateMany({
      data: { deletedBy: { set: [] } }
    });
    console.log('Fixed sessions:', res.count);
  } catch(e) {
    console.error(e);
  } finally {
    await prisma.\\$disconnect();
  }
}
run();
EOF
        node fix_all.cjs
      "
    `);
    console.log(result.stdout);
    
    ssh.dispose();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

main();
