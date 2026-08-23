/**
 * PM2 进程配置。
 * WebSocket 连接表在进程内存中，必须单实例；多实例会导致 pushToUser 丢消息。
 */
module.exports = {
  apps: [
    {
      name: 'lslife-api',
      script: './dist/index.js',
      instances: 1,
      exec_mode: 'fork',
      env: {
        NODE_ENV: 'production',
      },
    },
  ],
};
