const { NodeSSH } = require('node-ssh');
const ssh = new NodeSSH();
const path = require('path');

async function main() {
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  
  console.log('Uploading schema.prisma...');
  await ssh.putFile(
    path.join(__dirname, 'prisma/schema.prisma'),
    '/home/lslife/lslife-backend/prisma/schema.prisma'
  );

  console.log('Uploading seed.ts...');
  await ssh.putFile(
    path.join(__dirname, 'prisma/seed.ts'),
    '/home/lslife/lslife-backend/prisma/seed.ts'
  );

  console.log('Running clean prisma generate & seed...');
  const res = await ssh.execCommand('su - lslife -c "cd /home/lslife/lslife-backend && rm -rf node_modules/.prisma node_modules/@prisma && npm i && npx prisma generate && npm run seed"');
  console.log('Output:', res.stdout);
  console.log('Error:', res.stderr);

  ssh.dispose();
}
main();
