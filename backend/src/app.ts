import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { ok } from './lib/http.js';
import { notFound, errorHandler } from './middleware/error.js';

import authRoutes from './modules/auth.js';
import userRoutes from './modules/users.js';
import merchantRoutes from './modules/merchants.js';
import cartRoutes from './modules/cart.js';
import orderRoutes from './modules/orders.js';
import paymentRoutes from './modules/payments.js';
import publishRoutes from './modules/publish.js';
import membershipRoutes from './modules/membership.js';
import notificationRoutes from './modules/notifications.js';
import addressRoutes from './modules/addresses.js';
import aiRoutes from './modules/ai.js';
import adminRoutes from './modules/admin.js';
import uploadRoutes from './modules/upload.js';
import chatRoutes from './modules/chat.js';
import walletRoutes from './modules/wallet.js';

import categoryRoutes from './routes/category.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const uploadsDir = path.resolve(__dirname, '../public/uploads');
const assetsDir = path.resolve(__dirname, '../public/assets');
const publicDir = path.resolve(__dirname, '../public');
fs.mkdirSync(uploadsDir, { recursive: true });
fs.mkdirSync(assetsDir, { recursive: true });

export function createApp() {
  const app = express();

  app.use(helmet({ crossOriginResourcePolicy: false }));
  app.use(cors());
  app.use(express.json({ limit: '10mb' }));

  app.use('/uploads', express.static(uploadsDir));
  app.use('/assets', express.static(assetsDir));
  app.use('/public', express.static(publicDir));
  app.use('/api/uploads', express.static(uploadsDir));
  app.use('/api/assets', express.static(assetsDir));
  app.use('/api/public', express.static(publicDir));

  app.get('/api/health', (_req, res) => ok(res, { status: 'up', time: new Date().toISOString() }));

  app.get(['/api', '/api/'], (_req, res) =>
    ok(res, {
      name: '连山同城 LsLife API',
      version: '1.0.0',
      status: 'up',
      docs: {
        health: 'GET /api/health',
        categories: 'GET /api/categories/tree · GET /api/categories/:id/schema',
        merchants: 'GET /api/merchants',
        auth: 'POST /api/auth/register · POST /api/auth/login（手机号+密码）',
        posts: 'GET/POST /api/posts',
        upload: 'POST /api/upload',
        websocket: 'WSS /ws?token=<JWT>',
      },
    }),
  );

  app.use('/api/auth', authRoutes);
  app.use('/api/users', userRoutes);
  app.use('/api/categories', categoryRoutes);
  app.use('/api/merchants', merchantRoutes);
  app.use('/api/cart', cartRoutes);
  app.use('/api/orders', orderRoutes);
  app.use('/api/payments', paymentRoutes);
  app.use('/api/posts', publishRoutes);
  app.use('/api/membership', membershipRoutes);
  app.use('/api/notifications', notificationRoutes);
  app.use('/api/addresses', addressRoutes);
  app.use('/api/ai', aiRoutes);
  app.use('/api/admin', adminRoutes);
  app.use('/api/upload', uploadRoutes);
  app.use('/api/chat', chatRoutes);
  app.use('/api/wallet', walletRoutes);

  app.use(notFound);
  app.use(errorHandler);

  return app;
}
