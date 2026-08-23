import { NodeSSH } from 'node-ssh';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ssh = new NodeSSH();

const host = process.env.SSH_HOST || '115.191.6.95';
const username = process.env.SSH_USER || 'lslife';
const privateKeyPath =
  process.env.SSH_PRIVATE_KEY || path.join(process.env.USERPROFILE || process.env.HOME || '', '.ssh', 'id_ed25519');

async function main() {
  console.log(`Connecting to ${username}@${host} via key...`);
  await ssh.connect({
    host,
    username,
    privateKeyPath,
  });

  console.log('1. Creating backup and script directories on server...');
  await ssh.execCommand('mkdir -p /home/lslife/backups /home/lslife/lslife-backend/scripts');

  console.log('2. Uploading backup.sh...');
  await ssh.putFile(
    path.join(__dirname, 'scripts/backup.sh'),
    '/home/lslife/lslife-backend/scripts/backup.sh'
  );

  console.log('3. Setting executable permissions...');
  await ssh.execCommand('chmod +x /home/lslife/lslife-backend/scripts/backup.sh');

  console.log('4. Configuring Cron Job (Daily at 3 AM)...');
  // Remove any existing backup cron, then add new one
  const cronJob = '0 3 * * * /home/lslife/lslife-backend/scripts/backup.sh >> /home/lslife/backups/cron.log 2>&1';
  const installCronCmd = `
    (crontab -l 2>/dev/null | grep -v 'backup.sh'; echo "${cronJob}") | crontab -
  `;
  await ssh.execCommand(installCronCmd);
  
  console.log('5. Running the backup script NOW to test it (this may take a few seconds)...');
  const testRun = await ssh.execCommand('/home/lslife/lslife-backend/scripts/backup.sh');
  console.log('--- TEST RUN OUTPUT ---');
  console.log(testRun.stdout);
  if (testRun.stderr) console.error(testRun.stderr);
  console.log('-----------------------');

  if (testRun.code !== 0) {
    throw new Error(`Backup test run failed with code ${testRun.code}`);
  }

  console.log('✅ Backup deployment successful! Cron job is active and a test backup has been created.');
}

main()
  .catch((err) => {
    console.error('Error:', err);
    process.exitCode = 1;
  })
  .finally(() => ssh.dispose());
