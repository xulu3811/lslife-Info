import { env } from '../config/env.js';
import { dfaEngine } from './dfa.js';

export interface ModerationResult {
  pass: boolean;
  status: 'PUBLISHED' | 'MANUAL_REVIEWING' | 'REJECTED' | 'AI_REVIEWING';
  note?: string;
  matchedWords?: string[];
  level?: number;
}

/**
 * 文本风控初筛 (本地 DFA)
 */
export function moderateContent(title: string, description: string): ModerationResult {
  if (!env.contentModerationEnabled) {
    return { pass: true, status: 'PUBLISHED' };
  }
  
  const text = `${title} ${description}`;
  
  const dfaResult = dfaEngine.match(text);
  
  if (dfaResult.matched) {
    const blockedWords = dfaResult.words.filter(w => w.level >= 3);
    if (blockedWords.length > 0) {
      return { 
        pass: false, 
        status: 'REJECTED', 
        note: `触发严重违禁词: ${blockedWords.map(w => w.word).join(', ')}`,
        matchedWords: blockedWords.map(w => w.word),
        level: 3
      };
    }
    
    const reviewWords = dfaResult.words.filter(w => w.level === 2);
    if (reviewWords.length > 0) {
      return { 
        pass: true, 
        status: 'MANUAL_REVIEWING', 
        note: `包含可疑词汇: ${reviewWords.map(w => w.word).join(', ')}`,
        matchedWords: reviewWords.map(w => w.word),
        level: 2
      };
    }
  }
  
  return { pass: true, status: 'AI_REVIEWING', note: '等待 AI 引擎复核' };
}

import { getAiProvider } from './ai.js';
import { prisma } from '../lib/prisma.js';

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const UPLOAD_DIR = path.resolve(__dirname, '../../public/uploads');

async function triggerVisionReview(images: string[]): Promise<{ pass: boolean; status: string; reason: string }> {
  if (images.length === 0) return { pass: true, status: 'PUBLISHED', reason: '无图片' };
  if (!env.geminiApiKey) {
     return { pass: true, status: 'MANUAL_REVIEWING', reason: 'Gemini API Key 未配置，降级人工审核' };
  }
  
  try {
    const inlineDataList = [];
    for (const url of images) {
      if (url.includes('/uploads/')) {
        const filename = url.split('/').pop();
        if (filename) {
          const filePath = path.join(UPLOAD_DIR, filename);
          if (fs.existsSync(filePath)) {
            const base64 = fs.readFileSync(filePath).toString('base64');
            const ext = path.extname(filename).toLowerCase();
            const mimeType = ext === '.webp' ? 'image/webp' : ext === '.png' ? 'image/png' : 'image/jpeg';
            inlineDataList.push({
              inline_data: { mime_type: mimeType, data: base64 }
            });
          }
        }
      }
    }
    
    if (inlineDataList.length === 0) {
       return { pass: true, status: 'MANUAL_REVIEWING', reason: '无有效图片，转人工' };
    }

    const payload = {
      contents: [{
        parts: [
          { text: "作为本地生活平台审核员，请分析这些图片是否包含色情、血腥、暴恐、违禁品等违规内容。输出必须是严格的JSON格式：{\"pass\": boolean, \"status\": \"PUBLISHED\" | \"MANUAL_REVIEWING\" | \"REJECTED\", \"reason\": string}" },
          ...inlineDataList
        ]
      }],
      generationConfig: { response_mime_type: "application/json" }
    };
    
    const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${env.geminiApiKey}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    const data = await response.json() as any;
    const text = data.candidates?.[0]?.content?.parts?.[0]?.text || '{}';
    const parsed = JSON.parse(text);
    return {
      pass: !!parsed.pass,
      status: ['PUBLISHED', 'MANUAL_REVIEWING', 'REJECTED'].includes(parsed.status) ? parsed.status : 'MANUAL_REVIEWING',
      reason: parsed.reason || 'Gemini 检查完成'
    };
  } catch (e) {
    console.error('[Vision Review] Error:', e);
    return { pass: true, status: 'MANUAL_REVIEWING', reason: '视觉审查异常，降级人工' };
  }
}

/**
 * 异步触发双轨深度语义审查 (文本 + 图像)
 */
export async function triggerAiReview(postId: string, title: string, description: string, images: string[] = []) {
  try {
    const textReviewPromise = (async () => {
      const prompt = `你是一个本地生活服务平台的内容审核AI助手。
需要你对以下用户发布的商品/服务内容进行深度语义剖析（涉政、涉黄、暴恐、黑产等）。

标题: ${title}
内容描述: ${description}

请严格遵守只输出一个 JSON 对象，结构如下：
- "pass": boolean (是否通过)
- "status": "PUBLISHED" | "MANUAL_REVIEWING" | "REJECTED" (如果内容完全纯净合规，无任何违规/广告，返回 PUBLISHED；如果存在模糊内容或可疑商业引流，返回 MANUAL_REVIEWING；如果严重违规，返回 REJECTED)
- "reason": string (解释你的判断原因，特别提醒审核员需要注意的点)

JSON 结果：`;

      const result = await getAiProvider().generateText(prompt);
      
      let parsed: { pass: boolean; status: string; reason: string };
      try {
        const cleanJson = result.replace(/```json/g, '').replace(/```/g, '').trim();
        parsed = JSON.parse(cleanJson);
      } catch (e) {
        console.error('[AI Review] Failed to parse JSON response:', result);
        return { pass: true, status: 'MANUAL_REVIEWING', reason: '解析失败，降级人工' };
      }
      return {
        pass: parsed.pass,
        status: ['PUBLISHED', 'MANUAL_REVIEWING', 'REJECTED'].includes(parsed.status) ? parsed.status : 'MANUAL_REVIEWING',
        reason: parsed.reason
      };
    })();
    
    const [textResult, visionResult] = await Promise.all([textReviewPromise, triggerVisionReview(images)]);

    let finalStatus = 'PUBLISHED';
    if (textResult.status === 'REJECTED' || visionResult.status === 'REJECTED') {
      finalStatus = 'REJECTED';
    } else if (textResult.status === 'MANUAL_REVIEWING' || visionResult.status === 'MANUAL_REVIEWING') {
      finalStatus = 'MANUAL_REVIEWING';
    }

    const combinedNote = `[Text: ${textResult.reason}] [Vision: ${visionResult.reason}]`;
      
    await prisma.post.update({
      where: { id: postId },
      data: {
        status: finalStatus,
        reviewNote: combinedNote
      }
    });
    
    console.log(`[AI Review] Post ${postId} reviewed. Status -> ${finalStatus}`);
  } catch (error) {
    console.error(`[AI Review] Error processing post ${postId}:`, error);
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
