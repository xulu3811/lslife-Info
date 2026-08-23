import { env } from '../config/env.js';
import { prisma } from '../lib/prisma.js';

export interface AiRecommendation {
  merchantId: string;
  itemId: string;
  name: string;
  price: number;
}

export interface AiReply {
  reply: string;
  recommendations: AiRecommendation[];
}

/**
 * AI 同城助手抽象层。
 * 商业化约束: 中国大陆需使用境内合规大模型 (通义千问/文心一言/豆包),
 * 不使用 Gemini。此处保留 mock 与 dashscope(通义) 两种实现骨架。
 */
export interface AiProvider {
  recommend(prompt: string): Promise<AiReply>;
}

async function buildMerchantContext() {
  const merchants = await prisma.merchant.findMany({ include: { products: true } });
  return merchants.map((m) => ({
    id: m.externalId ?? m.id,
    name: m.name,
    tags: JSON.parse(m.tags) as string[],
    items: m.products.map((p) => ({ id: p.externalId ?? p.id, name: p.name, price: p.price, desc: p.desc })),
  }));
}

const mockProvider: AiProvider = {
  async recommend(prompt) {
    const merchants = await buildMerchantContext();
    const foodPicks = merchants
      .flatMap((m) => m.items.slice(0, 1).map((i) => ({ merchantId: m.id, itemId: i.id, name: i.name, price: i.price })))
      .slice(0, 2);
    return {
      reply:
        `您好！我是连山同城智能助手。收到您的需求「${prompt}」。\n\n` +
        '为您推荐连山壮族瑶族自治县的本地好物，点击卡片可直接下单体验哦！',
      recommendations: foodPicks,
    };
  },
};

const deepseekProvider: AiProvider = {
  async recommend(prompt) {
    const apiKey = env.aiApiKey || 'sk-30f79d21acbd487da71ec3cb5ce63d54';
    const merchants = await buildMerchantContext();
    const sys =
      '你是熟悉广东省清远市连山壮族瑶族自治县的同城生活专家。请基于给定商户数据做定制推荐, ' +
      '只输出合法 JSON: {"reply":"...","recommendations":[{"merchantId","itemId","name","price"}]}';
    const resp = await fetch('https://api.deepseek.com/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${apiKey}` },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: [
          { role: 'system', content: sys },
          { role: 'user', content: `商户数据:\n${JSON.stringify(merchants)}\n\n用户留言: ${prompt}` },
        ],
        response_format: { type: 'json_object' },
      }),
    });
    const data = (await resp.json()) as { choices?: Array<{ message?: { content?: string } }> };
    const text = data.choices?.[0]?.message?.content ?? '{}';
    return JSON.parse(text) as AiReply;
  },
};

export function getAiProvider(): AiProvider {
  switch (env.aiProvider) {
    case 'dashscope':
    case 'qianfan':
    case 'doubao':
    case 'deepseek':
      return deepseekProvider;
    default:
      return mockProvider;
  }
}
