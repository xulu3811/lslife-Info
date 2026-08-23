/**
 * 生产热修/运维脚本（通过 SSH 公钥登录，禁止在仓库中写入密码）。
 *
 * 用法（本机已配置 lslife@115.191.6.95 免密）:
 *   node deploy.mjs
 *
 * 可选环境变量:
 *   SSH_HOST=115.191.6.95
 *   SSH_USER=lslife
 *   SSH_PRIVATE_KEY=C:\\Users\\you\\.ssh\\id_ed25519
 */
import { NodeSSH } from 'node-ssh';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ssh = new NodeSSH();

const host = process.env.SSH_HOST || '115.191.6.95';
const username = process.env.SSH_USER || 'root';
const password = process.env.SSH_PASSWORD || 'Maxence2468;';

async function main() {
  console.log(`Connecting ${username}@${host} via password...`);
  await ssh.connect({
    host,
    username,
    password,
  });

  const targetDir = '/home/lslife/lslife-backend';

  console.log('Uploading hotfix files & entire src directory...');
  await ssh.putDirectory(path.join(__dirname, 'src'), `${targetDir}/src`, {
    recursive: true,
    concurrency: 10,
  });
  await ssh.putFiles([
    { local: path.join(__dirname, 'prisma/schema.prisma'), remote: `${targetDir}/prisma/schema.prisma` },
    { local: path.join(__dirname, 'package.json'), remote: `${targetDir}/package.json` },
    { local: path.join(__dirname, 'ecosystem.config.cjs'), remote: `${targetDir}/ecosystem.config.cjs` },
  ]);

  console.log('Build + reload PM2 (single instance)...');
  const result = await ssh.execCommand(
    [
      `cd ${targetDir}`,
      'export PATH=/home/lslife/.local/nodejs/bin:/usr/local/bin:/usr/bin:$PATH',
      'npm install',
      'npx prisma db push --accept-data-loss',
      'npx prisma generate',
      'npm run build',
      'pm2 delete lslife-api || true',
      'pm2 start ecosystem.config.cjs',
      'pm2 save',
      'pm2 list',
    ].join(' && '),
  );
  console.log(result.stdout || result.stderr);
  if (result.code !== 0) {
    throw new Error(`Remote command failed with code ${result.code}`);
  }

  console.log('Hotfix deployed.');
}

main()
  .catch((err) => {
    console.error('Error:', err);
    process.exitCode = 1;
  })
  .finally(() => ssh.dispose());
