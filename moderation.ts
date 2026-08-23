import { env } from '../config/env.js';

// 简易本地敏感词 (生产应接入阿里云/腾讯云内容安全 API + 人审队列)
const BLOCK_WORDS = ['黄赌毒', '诈骗', '暴力', '违法'];

export interface ModerationResult {
  pass: boolean;
  status: 'published' | 'pending_review' | 'rejected';
  note?: string;
}

/**
 * 内容审核。UGC 发布需机审 + 人审, 违规留存。
 * 生产升级: 调用大模型进行智能内容安全审核
 */
export async function moderateContent(title: string, description: string): Promise<ModerationResult> {
  const text = `${title} ${description}`;

  // 1. 如果配置了 AI 密钥，则使用大模型鉴黄暴恐
  if (env.aiApiKey) {
    try {
      const sys = '你是一个严格的 UGC 内容安全审核员。请判断以下内容是否包含黄赌毒、暴力、诈骗或违法违规信息。只输出合法 JSON: {"pass": boolean, "note": "如果违规，给出理由，否则为空"}';
      const resp = await fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${env.aiApiKey}` },
        body: JSON.stringify({
          model: env.aiModel || 'qwen-plus',
          messages: [
            { role: 'system', content: sys },
            { role: 'user', content: `审核内容:\n${text}` },
          ],
          response_format: { type: 'json_object' },
        }),
      });
      const data = (await resp.json()) as any;
      const content = data.choices?.[0]?.message?.content ?? '{}';
      const result = JSON.parse(content);
      
      if (!result.pass) {
        return { pass: false, status: 'rejected', note: `大模型风控拦截: ${result.note}` };
      }
      return { pass: true, status: 'published' };
    } catch (err) {
      console.error('[Moderation] AI 审核调用失败，降级到本地词库', err);
    }
  }

  // 2. 本地敏感词降级
  const hit = BLOCK_WORDS.find((w) => text.includes(w));
  if (hit) {
    return { pass: false, status: 'rejected', note: `命中违规词: ${hit}` };
  }
  return { pass: true, status: 'published' };
}
