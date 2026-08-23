import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  console.log('Connecting to 115.191.6.95...');
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('Uploading dist directory to /home/lslife/lslife-backend/dist ...');
  await ssh.putDirectory(
    'd:/LsLife/backend/dist',
    '/home/lslife/lslife-backend/dist',
    {
      recursive: true,
      concurrency: 10
    }
  );

  console.log('Restarting PM2...');
  const restartRes = await ssh.execCommand(
    'su - lslife -c "pm2 restart all"'
  );
  console.log('STDOUT:', restartRes.stdout);
  if (restartRes.stderr) console.error('STDERR:', restartRes.stderr);

  ssh.dispose();
  console.log('✅ Remote server updated and PM2 restarted!');
}

main().catch(err => {
  console.error('Failed:', err);
  process.exit(1);
});
