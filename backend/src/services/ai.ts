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
  generateText(prompt: string): Promise<string>;
}

async function buildMerchantContext() {
  const merchants = await prisma.merchant.findMany();
  return merchants.map((m) => ({
    id: m.externalId ?? m.id,
    name: m.name,
    tags: JSON.parse(m.tags) as string[],
    items: [] as any[],
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
  async generateText(prompt) {
    const hintMatch = prompt.match(/“(.+)”/);
    const hint = hintMatch ? hintMatch[1] : '这件宝贝';
    return `我这里有一款非常不错的${hint}，平时很爱惜，功能一切正常，配件齐全。\n因为近期用不上了，所以低价转让给有需要的朋友。\n\n支持验货，喜欢的话可以直接拍下或者私聊我！`;
  },
};

// TODO: 通义千问 DashScope (需 AI_API_KEY)
const dashscopeProvider: AiProvider = {
  async recommend(prompt) {
    if (!env.aiApiKey) throw new Error('AI_API_KEY 未配置');
    const merchants = await buildMerchantContext();
    const sys =
      '你是熟悉广东省清远市连山壮族瑶族自治县的同城生活专家。请基于给定商户数据做定制推荐, ' +
      '只输出合法 JSON: {"reply":"...","recommendations":[{"merchantId","itemId","name","price"}]}';
    const resp = await fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${env.aiApiKey}` },
      body: JSON.stringify({
        model: env.aiModel,
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
  async generateText(prompt) {
    if (!env.aiApiKey) throw new Error('AI_API_KEY 未配置');
    const resp = await fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${env.aiApiKey}` },
      body: JSON.stringify({
        model: env.aiModel,
        messages: [{ role: 'user', content: prompt }],
      }),
    });
    const data = (await resp.json()) as { choices?: Array<{ message?: { content?: string } }> };
    return data.choices?.[0]?.message?.content ?? '';
  },
};

const deepseekProvider: AiProvider = {
  async recommend(prompt) {
    if (!env.aiApiKey) {
      console.warn('[AI] AI_API_KEY 未配置，降级为 mock 推荐');
      return mockProvider.recommend(prompt);
    }
    const merchants = await buildMerchantContext();
    const sys =
      '你是熟悉广东省清远市连山壮族瑶族自治县的同城生活专家。请基于给定商户数据做定制推荐, ' +
      '只输出合法 JSON: {"reply":"...","recommendations":[{"merchantId":"","itemId":"","name":"","price":0}]}';
    const resp = await fetch('https://api.deepseek.com/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${env.aiApiKey}` },
      body: JSON.stringify({
        model: env.aiModel || 'deepseek-chat',
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
  async generateText(prompt) {
    if (!env.aiApiKey) throw new Error('AI_API_KEY 未配置，请在 .env 中设置');
    const resp = await fetch('https://api.deepseek.com/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${env.aiApiKey}` },
      body: JSON.stringify({
        model: env.aiModel || 'deepseek-chat',
        messages: [{ role: 'user', content: prompt }],
        response_format: { type: 'json_object' }
      }),
    });
    const data = (await resp.json()) as any;
    return data.choices?.[0]?.message?.content ?? '{}';
  },
};

export function getAiProvider(): AiProvider {
  return deepseekProvider;
}
