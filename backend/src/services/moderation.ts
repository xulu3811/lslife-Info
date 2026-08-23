import { env } from '../config/env.js';

/** 明确违规词（整词/短语），避免单字误伤如「香蕉」 */
const BLOCK_WORDS = [
  '黄赌毒',
  '色情',
  '嫖娼',
  '赌场',
  '毒品',
  '冰毒',
  '海洛因',
  '代考',
  '办证',
  '洗钱',
  '走私',
  '刷单返利',
  '原味内裤',
];

/** 可疑内容进入人工审核 */
const REVIEW_WORDS = ['发票', '套现', '枪手', '代练账号'];

export interface ModerationResult {
  pass: boolean;
  status: 'PUBLISHED' | 'AI_REVIEWING' | 'MANUAL_REVIEWING' | 'REJECTED';
  note?: string;
}

/**
 * 内容审核（本地词表 DFA）。
 * - 命中 BLOCK → REJECTED（直接拒绝）
 * - 命中 REVIEW → MANUAL_REVIEWING（进入人工审核）
 * - 其余 → AI_REVIEWING（进入异步 AI 深度审查队列）
 */
export function moderateContent(title: string, description: string): ModerationResult {
  if (!env.contentModerationEnabled) {
    return { pass: true, status: 'PUBLISHED' };
  }
  const text = `${title} ${description}`;
  const blocked = BLOCK_WORDS.find((w) => text.includes(w));
  if (blocked) {
    return { pass: false, status: 'REJECTED', note: `命中本地违规词: ${blocked}` };
  }
  const review = REVIEW_WORDS.find((w) => text.includes(w));
  if (review) {
    return { pass: true, status: 'MANUAL_REVIEWING', note: `本地可疑内容待审: ${review}` };
  }
  return { pass: true, status: 'AI_REVIEWING', note: '等待 AI 深度审核' };
}

import { getAiProvider } from './ai.js';
import { prisma } from '../lib/prisma.js';

/**
 * 异步触发 DeepSeek 大模型深度语义审查
 */
export async function triggerAiReview(postId: string, title: string, description: string) {
  try {
    const prompt = `你是一个本地生活服务平台的内容审核AI助手。
需要你对以下用户发布的商品/服务内容进行深度语义剖析（涉政、涉黄、暴恐、黑产等）。

标题: ${title}
内容描述: ${description}

请严格遵守只输出一个 JSON 对象，结构如下：
- "pass": boolean (是否通过)
- "status": "MANUAL_REVIEWING" | "REJECTED" (即使内容完全合规，也必须返回 MANUAL_REVIEWING 进入人工终审环节；如果严重违规，返回 REJECTED)
- "reason": string (解释你的判断原因，特别提醒审核员需要注意的点)

JSON 结果：`;

    const result = await getAiProvider().generateText(prompt);
    
    // 解析 AI 响应
    let parsed: { pass: boolean; status: string; reason: string };
    try {
      // 清理 AI 响应中的 markdown 标记
      const cleanJson = result.replace(/```json/g, '').replace(/```/g, '').trim();
      parsed = JSON.parse(cleanJson);
      // 强制约束状态：不能自动发布
      if (parsed.status === 'PUBLISHED') {
        parsed.status = 'MANUAL_REVIEWING';
      }
    } catch (e) {
      console.error('[AI Review] Failed to parse JSON response:', result);
      // 解析失败降级为人工审核
      await prisma.post.update({
        where: { id: postId },
        data: { status: 'MANUAL_REVIEWING', reviewNote: 'AI 审核异常，降级转人工' }
      });
      return;
    }

    // 更新数据库
    const newStatus = ['PUBLISHED', 'MANUAL_REVIEWING', 'REJECTED'].includes(parsed.status) 
      ? parsed.status 
      : 'MANUAL_REVIEWING';
      
    await prisma.post.update({
      where: { id: postId },
      data: {
        status: newStatus,
        reviewNote: `[DeepSeek AI] ${parsed.reason}`
      }
    });
    
    console.log(`[AI Review] Post ${postId} reviewed. Status -> ${newStatus}`);
  } catch (error) {
    console.error(`[AI Review] Error processing post ${postId}:`, error);
    // 网络异常等降级转人工
    await prisma.post.update({
      where: { id: postId },
      data: { status: 'MANUAL_REVIEWING', reviewNote: 'AI 服务调用失败，降级转人工' }
    });
  }
}

/**
 * 异步触发 DeepSeek 对用户昵称的审查
 */
export async function triggerProfileAiReview(userId: string, nickname: string | null) {
  if (!nickname) {
    // 仅修改头像，直接进入人工审核
    await prisma.user.update({
      where: { id: userId },
      data: { profileReviewStatus: 'MANUAL_REVIEWING', profileReviewNote: '自动进入头像人工终审' }
    });
    return;
  }

  try {
    const prompt = `你是一个本地生活服务平台的内容审核AI助手。
需要你对以下用户的昵称进行深度语义剖析（涉政、涉黄、暴恐、黑产等）。

昵称: ${nickname}

请严格遵守只输出一个 JSON 对象，结构如下：
- "pass": boolean (是否通过)
- "status": "MANUAL_REVIEWING" | "REJECTED" (如果合规，请返回 MANUAL_REVIEWING 进入人工头像与昵称终审环节；如果严重违规，返回 REJECTED)
- "reason": string (解释你的判断原因，特别提醒审核员需要注意的点)

JSON 结果：`;

    const result = await getAiProvider().generateText(prompt);
    
    let parsed: { pass: boolean; status: string; reason: string };
    try {
      const cleanJson = result.replace(/```json/g, '').replace(/```/g, '').trim();
      parsed = JSON.parse(cleanJson);
      if (parsed.status === 'PUBLISHED') {
        parsed.status = 'MANUAL_REVIEWING';
      }
    } catch (e) {
      console.error('[AI Review] Failed to parse profile JSON response:', result);
      await prisma.user.update({
        where: { id: userId },
        data: { profileReviewStatus: 'MANUAL_REVIEWING', profileReviewNote: 'AI 审核异常，降级转人工' }
      });
      return;
    }

    const newStatus = ['MANUAL_REVIEWING', 'REJECTED'].includes(parsed.status) 
      ? parsed.status 
      : 'MANUAL_REVIEWING';
      
    await prisma.user.update({
      where: { id: userId },
      data: {
        profileReviewStatus: newStatus,
        profileReviewNote: `[DeepSeek AI] ${parsed.reason}`
      }
    });
    
    console.log(`[AI Review] User profile ${userId} reviewed. Status -> ${newStatus}`);
  } catch (error) {
    console.error(`[AI Review] Error processing user profile ${userId}:`, error);
    await prisma.user.update({
      where: { id: userId },
      data: { profileReviewStatus: 'MANUAL_REVIEWING', profileReviewNote: 'AI 服务调用失败，降级转人工' }
    });
  }
}
