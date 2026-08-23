import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  console.log('连接远程服务器 115.191.6.95...');
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });
  console.log('已连接。推送 dist 目录至 /home/lslife/lslife-backend/dist ...');

  // 上传 dist 目录到 /home/lslife/lslife-backend/dist
  await ssh.putDirectory('./dist', '/home/lslife/lslife-backend/dist', {
    recursive: true,
    concurrency: 10,
  });

  console.log('dist 上传完成。重启 PM2 服务 (lslife-api)...');
  const res = await ssh.execCommand('export PATH=$PATH:/home/lslife/.local/nodejs/bin; pm2 restart lslife-api --update-env', { cwd: '/home/lslife/lslife-backend' });
  console.log('PM2 输出:', res.stdout || res.stderr);

  console.log('检查 PM2 状态...');
  const statusRes = await ssh.execCommand('export PATH=$PATH:/home/lslife/.local/nodejs/bin; pm2 info lslife-api');
  console.log(statusRes.stdout);

  console.log('测试 DELETE /api/chat/sessions/test...');
  const testRes = await ssh.execCommand('curl -i -X DELETE http://127.0.0.1:4000/api/chat/sessions/test');
  console.log('DELETE 接口 HTTP 响应:\n', testRes.stdout);

  ssh.dispose();
  console.log('部署完成！');
}

main().catch(err => {
  console.error('部署失败:', err);
  process.exit(1);
});
