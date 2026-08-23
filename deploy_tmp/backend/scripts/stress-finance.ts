import axios from 'axios';
import assert from 'assert';

const BASE = process.env.BASE || 'http://localhost:3000';

async function call<T>(method: string, path: string, data?: any, token?: string): Promise<T> {
  const headers: any = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  
  try {
    const res = await axios({ method, url: `${BASE}${path}`, data, headers });
    return res.data.data;
  } catch (err: any) {
    console.error(`  ✗ ${method} ${path}:`, err.response?.data?.message || err.message);
    throw err;
  }
}

async function run() {
  console.log(`资金与订单财务链路测试开始 BASE=${BASE}`);
  
  const phone = `139${Math.floor(Math.random() * 100000000).toString().padStart(8, '0')}`;
  
  // 1. 注册
  const password = `Finance${100000 + Math.floor(Math.random() * 90000)}`;
  const reg = await call<{ token: string }>('POST', '/auth/register', {
    phone,
    password,
    nickname: '财务测试用户'
  });
  const token = reg.token;
  console.log(`  ✓ 注册测试用户 ${phone}`);

  // 2. 获取初始商品，新建地址
  const merchantsData = await call<{ list: any[] }>('GET', '/merchants?pageSize=50');
  const merchant = merchantsData.list.find(m => m.items && m.items.length > 0);
  if (!merchant) throw new Error('没有带商品的商家');
  
  const product = merchant.items[0];

  await call('POST', '/addresses', {
    name: '李老板', phone, address: '财务压测路', latitude: 0, longitude: 0
  }, token);

  // 3. 积分充值 10元 -> 100积分
  console.log(`  - 开始充值 10 元 (100积分)`);
  const rechargeRes = await call<{ paymentId: string, prepayPayload: any }>('POST', '/payments/recharge', {
    amount: 10, channel: 'mock'
  }, token);
  
  await call('POST', '/payments/mock-confirm', { orderNo: rechargeRes.prepayPayload.orderNo }, token);
  
  let u = await call<any>('GET', '/auth/me', undefined, token);
  assert(u.points === 100, `积分充值失败，当前积分: ${u.points}`);
  console.log(`  ✓ 充值成功，当前积分: ${u.points}`);

  // 4. 创建订单 (假设商品价格 20)
  // 如果商品不够 20，我们就买多件
  const quantity = Math.ceil(20 / product.price);
  
  const orderRes = await call<{ id: string, orderNo: string }>('POST', '/orders', {
    merchantId: merchant.id,
    items: [{ productId: product.id, quantity }],
    deliveryAddress: { name: '李老板', phone, address: '财务压测路' }
  }, token);

  console.log(`  - 下单成功，订单总额: ${product.price * quantity}`);

  // 5. 混合支付: 使用 100 积分 (抵扣 10元)，剩余使用 mock 微信支付
  const payCreate = await call<{ paymentId: string, prepayPayload: any, paid?: boolean }>('POST', '/payments/create', {
    orderId: orderRes.id,
    pointsUsed: 100,
    channel: 'mock'
  }, token);
  
  if (!payCreate.paid) {
    await call('POST', '/payments/mock-confirm', { orderNo: payCreate.prepayPayload.orderNo }, token);
  }

  u = await call<any>('GET', '/auth/me', undefined, token);
  assert(u.points === 0, `积分抵扣失败，当前积分: ${u.points}`);
  console.log(`  ✓ 混合支付成功，积分抵扣完成`);

  console.log('财务链路测试通过 ✅');
}

run().catch((e) => {
  console.error('Test Failed:', e);
  process.exit(1);
});
