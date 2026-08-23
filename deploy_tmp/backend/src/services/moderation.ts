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
  status: 'published' | 'pending_review' | 'rejected';
  note?: string;
}

/**
 * 内容审核（本地词表）。
 * - 命中 BLOCK → rejected（不入库额度）
 * - 命中 REVIEW → pending_review（可进管理后台）
 * - 其余 → published（信息流可见，保证发布链路可跑通）
 */
export function moderateContent(title: string, description: string): ModerationResult {
  if (!env.contentModerationEnabled) {
    return { pass: true, status: 'published' };
  }
  const text = `${title} ${description}`;
  const blocked = BLOCK_WORDS.find((w) => text.includes(w));
  if (blocked) {
    return { pass: false, status: 'rejected', note: `命中违规词: ${blocked}` };
  }
  const review = REVIEW_WORDS.find((w) => text.includes(w));
  if (review) {
    return { pass: true, status: 'pending_review', note: `可疑内容待审: ${review}` };
  }
  return { pass: true, status: 'published', note: '自动审核通过' };
}
