/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import express from 'express';
import path from 'path';
import { createServer as createViteServer } from 'vite';
import dotenv from 'dotenv';
import { GoogleGenAI } from '@google/genai';
import { merchantsData, USER_INITIAL_LOCATION } from './src/data/merchants.js';

dotenv.config();

const app = express();
const PORT = 3000;

// Initialize Gemini Client
const ai = process.env.GEMINI_API_KEY
  ? new GoogleGenAI({
      apiKey: process.env.GEMINI_API_KEY,
      httpOptions: {
        headers: {
          'User-Agent': 'aistudio-build',
        },
      },
    })
  : null;

app.use(express.json());

// In-memory order storage
interface DBOrder {
  id: string;
  merchantId: string;
  merchantName: string;
  merchantLogo: string;
  items: any[];
  totalAmount: number;
  deliveryFee: number;
  paymentMethod: 'alipay' | 'wechat' | 'wallet';
  deliveryAddress: {
    name: string;
    phone: string;
    address: string;
  };
  createdAt: string; // ISO string
}

const ordersDb: Record<string, DBOrder> = {};

// API Endpoint: Get all merchants
app.get('/api/merchants', (req, res) => {
  res.json(merchantsData);
});

// API Endpoint: Get single merchant
app.get('/api/merchants/:id', (req, res) => {
  const merchant = merchantsData.find((m) => m.id === req.params.id);
  if (!merchant) {
    res.status(404).json({ error: 'Merchant not found' });
    return;
  }
  res.json(merchant);
});

// API Endpoint: Create a new order
app.post('/api/orders', (req, res) => {
  const { merchantId, items, totalAmount, deliveryFee, paymentMethod, deliveryAddress } = req.body;
  
  const merchant = merchantsData.find((m) => m.id === merchantId);
  if (!merchant) {
    res.status(400).json({ error: 'Invalid merchant ID' });
    return;
  }

  const orderId = 'LS' + Math.floor(Math.random() * 900000 + 100000);
  const newOrder: DBOrder = {
    id: orderId,
    merchantId,
    merchantName: merchant.name,
    merchantLogo: merchant.logo,
    items,
    totalAmount,
    deliveryFee,
    paymentMethod,
    deliveryAddress,
    createdAt: new Date().toISOString(),
  };

  ordersDb[orderId] = newOrder;
  res.json({ success: true, order: newOrder });
});

// API Endpoint: Get all orders history
app.get('/api/orders', (req, res) => {
  const PREPARING_TIME = 15;
  const DELIVERING_TIME = 45;
  const TOTAL_TIME = PREPARING_TIME + DELIVERING_TIME;

  const allOrders = Object.values(ordersDb).map(order => {
    const now = new Date();
    const createdAt = new Date(order.createdAt);
    const elapsedSeconds = Math.floor((now.getTime() - createdAt.getTime()) / 1000);
    
    let status: 'pending' | 'preparing' | 'delivering' | 'delivered' = 'preparing';
    if (elapsedSeconds < PREPARING_TIME) {
      status = 'preparing';
    } else if (elapsedSeconds < TOTAL_TIME) {
      status = 'delivering';
    } else {
      status = 'delivered';
    }
    
    return {
      ...order,
      status
    };
  }).sort((a, b) => {
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
  });
  res.json(allOrders);
});

// API Endpoint: Get single order details & live delivery tracking
app.get('/api/orders/:id', (req, res) => {
  const order = ordersDb[req.params.id];
  if (!order) {
    res.status(404).json({ error: 'Order not found' });
    return;
  }

  const merchant = merchantsData.find((m) => m.id === order.merchantId);
  if (!merchant) {
    res.status(404).json({ error: 'Merchant not found' });
    return;
  }

  // Calculate live delivery state based on time elapsed since creation
  const now = new Date();
  const createdAt = new Date(order.createdAt);
  const elapsedSeconds = Math.floor((now.getTime() - createdAt.getTime()) / 1000);

  let status: 'pending' | 'preparing' | 'delivering' | 'delivered' = 'preparing';
  let riderLat = merchant.latitude;
  let riderLng = merchant.longitude;
  let progress = 0;

  const PREPARING_TIME = 15; // seconds for demo
  const DELIVERING_TIME = 45; // seconds for demo
  const TOTAL_TIME = PREPARING_TIME + DELIVERING_TIME;

  if (elapsedSeconds < PREPARING_TIME) {
    status = 'preparing';
    progress = Math.min(100, Math.round((elapsedSeconds / PREPARING_TIME) * 100));
  } else if (elapsedSeconds < TOTAL_TIME) {
    status = 'delivering';
    const deliveryElapsed = elapsedSeconds - PREPARING_TIME;
    const ratio = deliveryElapsed / DELIVERING_TIME;
    progress = Math.min(100, Math.round(ratio * 100));
    
    // Linearly interpolate rider location from merchant to user location
    riderLat = merchant.latitude + (USER_INITIAL_LOCATION.lat - merchant.latitude) * ratio;
    riderLng = merchant.longitude + (USER_INITIAL_LOCATION.lng - merchant.longitude) * ratio;
  } else {
    status = 'delivered';
    progress = 100;
    riderLat = USER_INITIAL_LOCATION.lat;
    riderLng = USER_INITIAL_LOCATION.lng;
  }

  res.json({
    ...order,
    status,
    progress,
    rider: {
      name: '阿力 (连山特派骑手)',
      phone: '139-2244-8800',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80',
      lat: riderLat,
      lng: riderLng,
    },
    secondsRemaining: Math.max(0, TOTAL_TIME - elapsedSeconds),
  });
});

// API Endpoint: AI-Powered local food recommendation
app.post('/api/gemini/recommend', async (req, res) => {
  const { prompt } = req.body;
  if (!prompt) {
    res.status(400).json({ error: 'Prompt is required' });
    return;
  }

  if (!ai) {
    // Fallback if API key is not available
    res.json({
      reply: '您好！我是连山同城智能小助手。目前AI服务连接中，我可以直接向您推荐连山壮族瑶族自治县的特色名吃：\n\n1. **瑶家柴火鸡**：纯天然柴火烹制，鸡肉饱满香脆。\n2. **连山大汤糍**：传统咸甜大汤糍，老街口碑之王，软糯香滑。\n3. **腊肉冬笋竹筒饭**：竹香与农家熏腊肉完美融合！\n\n请问您今天胃口如何？点击菜单可以直接下单体验哦！',
      recommendations: [
        { merchantId: 'm1', itemId: 'm1_i1', name: '招牌瑶家柴火鸡', price: 68 },
        { merchantId: 'm2', itemId: 'm2_i1', name: '招牌经典咸大汤糍', price: 10 }
      ]
    });
    return;
  }

  try {
    const formattedMerchants = merchantsData.map(m => ({
      id: m.id,
      name: m.name,
      tags: m.tags,
      items: m.items.map(i => ({ id: i.id, name: i.name, price: i.price, desc: i.desc }))
    }));

    const response = await ai.models.generateContent({
      model: 'gemini-3.5-flash',
      contents: `你是一个熟悉广东省清远市连山壮族瑶族自治县美食的同城生活专家。请根据用户的喜好，在下方提供的真实商户数据中，进行定制化、温情并充满粤北特色的菜品推荐。

商户数据:
${JSON.stringify(formattedMerchants, null, 2)}

用户留言: "${prompt}"

请返回一个包含推荐文字和推荐菜品ID列表的JSON数据，格式如下：
{
  "reply": "带有连山风土人情和诱人美食描述的回答...",
  "recommendations": [
    { "merchantId": "商户ID", "itemId": "菜品ID", "name": "菜品名称", "price": 价格 }
  ]
}
不要输出任何Markdown代码块标记，只输出一个单纯的、符合该格式的合法的JSON字符串。`,
      config: {
        responseMimeType: 'application/json',
      },
    });

    const text = response.text || '{}';
    res.json(JSON.parse(text));
  } catch (error) {
    console.error('Gemini error:', error);
    res.status(500).json({ error: 'Failed to generate recommendation' });
  }
});

// Vite Setup for serving client frontend
async function startServer() {
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
  });
}

startServer();
