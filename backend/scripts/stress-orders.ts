/**
 * 订单链路压力测试（加购物车 + 结算下单）
 * 用法: BASE=https://mentalhlp.site/api CONCURRENCY=10 ROUNDS=3 npx tsx scripts/stress-orders.ts
 */
const BASE = process.env.BASE ?? 'http://localhost:4000/api';
const CONCURRENCY = Number(process.env.CONCURRENCY ?? 10);
const ROUNDS = Number(process.env.ROUNDS ?? 3);

type Envelope<T> = { code: number; message: string; data: T };

async function call<T>(method: string, path: string, body?: unknown, token?: string): Promise<T> {
  const res = await fetch(BASE + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const json = (await res.json()) as Envelope<T>;
  if (json.code !== 0) throw new Error(`${method} ${path}: ${json.message}`);
  return json.data;
}

let productPool: { id: string; merchantId: string }[] = [];

async function initProductPool() {
  const data = await call<{ list: Array<{ id: string; items: Array<{ id: string }> }> }>('GET', '/merchants?pageSize=50');
  
  for (const m of data.list) {
    if (m.items && m.items.length > 0) {
      for (const p of m.items) {
        productPool.push({ id: p.id, merchantId: m.id });
      }
    }
  }
  
  if (productPool.length === 0) {
    throw new Error('当前环境无可用的商品进行测试，请先上传商品');
  }
}

async function oneUserFlow(i: number) {
  const phone = `137${String(10000000 + ((Date.now() + i * 131) % 89999999)).slice(0, 8)}`;
  const password = `Order${100000 + i}`;
  const t0 = Date.now();
  
  // 1. 注册
  const reg = await call<{ token: string }>('POST', '/auth/register', {
    phone,
    password,
    nickname: `下单压测${i}`,
  });
  const token = reg.token;

  // 2. 挑选随机商品
  const product = productPool[Math.floor(Math.random() * productPool.length)];

  // 3. 加入购物车
  await call('POST', '/cart', {
    productId: product.id,
    merchantId: product.merchantId,
    quantity: 1
  }, token);

  // 4. 获取购物车记录用于结算
  const cart = await call<{ id: string }[]>('GET', '/cart', undefined, token);

  // 5. 新建地址
  await call<{ id: string }>('POST', '/addresses', {
    name: `压测收货人${i}`,
    phone,
    address: '连山吉田镇压测路1号',
    latitude: 24.502,
    longitude: 112.085
  }, token);

  // 6. 结算下单
  const checkoutResult = await call<{ id: string; orderNo: string }>('POST', '/orders', {
    merchantId: product.merchantId,
    items: [{ productId: product.id, quantity: 1 }],
    deliveryAddress: { name: `压测收货人${i}`, phone, address: '连山吉田镇压测路1号' }
  }, token);

  // 7. 模拟支付
  await call('POST', `/payments/mock-confirm`, { orderNo: checkoutResult.orderNo }, token);

  return { i, ms: Date.now() - t0, orderId: checkoutResult.id };
}

async function main() {
  console.log(`订单压测 BASE=${BASE} CONCURRENCY=${CONCURRENCY} ROUNDS=${ROUNDS}`);
  
  await initProductPool();
  console.log(`载入商品池，共 ${productPool.length} 个商品...`);

  const allMs: number[] = [];
  let ok = 0;
  let fail = 0;

  for (let r = 0; r < ROUNDS; r++) {
    const tasks = Array.from({ length: CONCURRENCY }, (_, j) => oneUserFlow(r * CONCURRENCY + j));
    const settled = await Promise.allSettled(tasks);
    for (const s of settled) {
      if (s.status === 'fulfilled') {
        ok += 1;
        allMs.push(s.value.ms);
        console.log(`  ✓ #${s.value.i} ${s.value.ms}ms orderId=${s.value.orderId}`);
      } else {
        fail += 1;
        console.log(`  ✗ ${s.reason?.message || s.reason}`);
      }
    }
  }

  allMs.sort((a, b) => a - b);
  const p50 = allMs[Math.floor(allMs.length * 0.5)] ?? 0;
  const p95 = allMs[Math.floor(allMs.length * 0.95)] ?? 0;
  const avg = allMs.length ? Math.round(allMs.reduce((a, b) => a + b, 0) / allMs.length) : 0;
  console.log('\n==== 压测结果 ====');
  console.log(`成功 ${ok} / 失败 ${fail}`);
  console.log(`延迟 avg=${avg}ms p50=${p50}ms p95=${p95}ms`);
  if (fail > 0 || ok === 0) process.exit(1);
  console.log('订单压测通过 ✅');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
