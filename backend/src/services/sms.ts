import { env } from '../config/env.js';

/**
 * 短信服务抽象层。
 * 本地/演示使用 mock (验证码打印到控制台并回传, 方便联调)。
 * 生产切换 SMS_PROVIDER=aliyun/tencent 并实现对应 send。
 */
export interface SmsProvider {
  send(phone: string, code: string): Promise<{ mockCode?: string }>;
}

const mockProvider: SmsProvider = {
  async send(phone, code) {
    console.log(`[SMS:mock] 向 ${phone} 发送验证码: ${code}`);
    return { mockCode: code };
  },
};

// TODO: 接入阿里云/腾讯云短信 (需企业资质与短信签名/模板审核)
const aliyunProvider: SmsProvider = {
  async send(phone, code) {
    console.log(`[SMS:aliyun] TODO 调用阿里云短信 API 发送 ${phone} -> ${code}`);
    return {};
  },
};

export function getSmsProvider(): SmsProvider {
  switch (env.smsProvider) {
    case 'aliyun':
    case 'tencent':
      return aliyunProvider;
    default:
      return mockProvider;
  }
}
