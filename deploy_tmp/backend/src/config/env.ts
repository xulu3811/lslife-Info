import dotenv from 'dotenv';

dotenv.config();

function required(key: string, fallback?: string): string {
  const value = process.env[key] ?? fallback;
  if (value === undefined) {
    throw new Error(`Missing required env var: ${key}`);
  }
  return value;
}

export const env = {
  port: Number(process.env.PORT ?? 4000),
  jwtSecret: required('JWT_SECRET', 'dev-secret-change-me'),
  jwtExpiresIn: process.env.JWT_EXPIRES_IN ?? '30d',
  smsProvider: process.env.SMS_PROVIDER ?? 'mock',
  payProvider: process.env.PAY_PROVIDER ?? 'mock',
  aiProvider: process.env.AI_PROVIDER ?? 'mock',
  aiApiKey: process.env.AI_API_KEY ?? '',
  aiModel: process.env.AI_MODEL ?? 'qwen-plus',
  contentModerationEnabled: (process.env.CONTENT_MODERATION_ENABLED ?? 'true') === 'true',
  isProd: process.env.NODE_ENV === 'production',
};
