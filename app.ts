import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { ok } from './lib/http.js';
import { notFound, errorHandler } from './middleware/error.js';

import authRoutes from './modules/auth.js';
import merchantRoutes from './modules/merchants.js';
import cartRoutes from './modules/cart.js';
import orderRoutes from './modules/orders.js';
import paymentRoutes from './modules/payments.js';
import publishRoutes from './modules/publish.js';
import membershipRoutes from './modules/membership.js';
import notificationRoutes from './modules/notifications.js';
import addressRoutes from './modules/addresses.js';
import aiRoutes from './modules/ai.js';
import riderRoutes from './modules/rider.js';

export function createApp() {
  const app = express();

  app.use(helmet());
  app.use(cors());
  app.use(express.json({ limit: '10mb' }));

  app.get('/api/health', (_req, res) => ok(res, { status: 'up', time: new Date().toISOString() }));

  // 浏览器直接打开 /api 或 /api/ 时给出友好指引 (非业务接口)
  app.get(['/api', '/api/'], (_req, res) =>
    ok(res, {
      name: '连山同城 LsLife API',
      version: '1.0.0',
      status: 'up',
      docs: {
        health: 'GET /api/health',
        merchants: 'GET /api/merchants',
        auth: 'POST /api/auth/send-code · POST /api/auth/login',
        websocket: 'WSS /ws?token=<JWT>',
      },
    }),
  );

  app.use('/api/auth', authRoutes);
  app.use('/api/merchants', merchantRoutes);
  app.use('/api/cart', cartRoutes);
  app.use('/api/orders', orderRoutes);
  app.use('/api/payments', paymentRoutes);
  app.use('/api/posts', publishRoutes);
  app.use('/api/membership', membershipRoutes);
  app.use('/api/notifications', notificationRoutes);
  app.use('/api/addresses', addressRoutes);
  app.use('/api/ai', aiRoutes);
  app.use('/api/rider', riderRoutes);

  app.use(notFound);
  app.use(errorHandler);

  return app;
}
