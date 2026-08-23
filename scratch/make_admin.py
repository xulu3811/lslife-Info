import paramiko

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect('115.191.6.95', username='root', password='Maxence2468;')

node_script = """
import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();
async function run() {
  const result = await prisma.user.updateMany({ data: { role: 'ADMIN' } });
  console.log('Updated ' + result.count + ' users to ADMIN.');
  process.exit(0);
}
run();
"""

s, out, err = client.exec_command(f'su - lslife -c "cat << \'EOF\' > /home/lslife/lslife-backend/make_admin.ts\n{node_script}\nEOF"')
s, out, err = client.exec_command('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:\\$PATH && cd /home/lslife/lslife-backend && npx tsx make_admin.ts"')

print('OUT:', out.read().decode('utf-8', errors='ignore'))
print('ERR:', err.read().decode('utf-8', errors='ignore'))
