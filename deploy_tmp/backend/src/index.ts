import http from 'node:http';
import { createApp } from './app.js';
import { attachRealtime } from './realtime/hub.js';
import { env } from './config/env.js';

const app = createApp();
const server = http.createServer(app);
attachRealtime(server);

server.listen(env.port, () => {
  console.log(`\n连山同城 LsLife 后端已启动`);
  console.log(`  HTTP  : http://localhost:${env.port}/api`);
  console.log(`  WS    : ws://localhost:${env.port}/ws?token=<JWT>`);
  console.log(`  健康检查: http://localhost:${env.port}/api/health\n`);
});
