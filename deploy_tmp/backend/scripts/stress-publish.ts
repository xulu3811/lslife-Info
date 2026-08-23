/**
 * 发布链路压力测试（真实打 API + 写库）
 * 用法: BASE=https://mentalhlp.site/api CONCURRENCY=20 ROUNDS=5 npx tsx scripts/stress-publish.ts
 */
const BASE = process.env.BASE ?? 'http://localhost:4000/api';
const CONCURRENCY = Number(process.env.CONCURRENCY ?? 10);
const ROUNDS = Number(process.env.ROUNDS ?? 3);
const DEMO_IMG = 'https://images.unsplash.com/photo-1485965120186-bdfc4c6e3e1b?w=400';

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

async function oneUserFlow(i: number) {
  const phone = `139${String(10000000 + ((Date.now() + i * 97) % 89999999)).slice(0, 8)}`;
  const password = `Stress${100000 + i}`;
  const t0 = Date.now();
  const reg = await call<{ token: string }>('POST', '/auth/register', {
    phone,
    password,
    nickname: `压测${i}`,
  });
  const token = reg.token;
  const post = await call<{ id: string; status: string }>('POST', '/posts', {
    category: 'second_hand',
    description: `压测商品 #${i} 九成新闲置，功能正常，连山自提优先。`,
    price: 10 + (i % 50),
    images: [DEMO_IMG],
    brand: '其他',
    condition: '几乎全新',
    shipping: '自提',
    locationName: '连山壮族瑶族自治县',
  }, token);
  if (post.status !== 'published') throw new Error(`unexpected status ${post.status}`);
  const feed = await call<{ list: Array<{ id: string }> }>('GET', `/posts?category=second_hand&pageSize=20`, undefined, token);
  const visible = feed.list.some((p) => p.id === post.id);
  const quota = await call<{ used: number }>('GET', '/posts/quota', undefined, token);
  return { i, ms: Date.now() - t0, postId: post.id, visible, used: quota.used };
}

async function main() {
  console.log(`发布压测 BASE=${BASE} CONCURRENCY=${CONCURRENCY} ROUNDS=${ROUNDS}`);
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
        console.log(`  ✓ #${s.value.i} ${s.value.ms}ms post=${s.value.postId} feed=${s.value.visible}`);
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
  console.log('发布压测通过 ✅');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
