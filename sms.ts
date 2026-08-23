import { env } from '../config/env.js';
import Core from '@alicloud/pop-core';

export interface SmsProvider {
  send(phone: string, code: string): Promise<{ mockCode?: string }>;
}

const mockProvider: SmsProvider = {
  async send(phone, code) {
    console.log(`[SMS:mock] 向 ${phone} 发送验证码: ${code}`);
    return { mockCode: code };
  },
};

const aliyunProvider: SmsProvider = {
  async send(phone, code) {
    const accessKeyId = process.env.ALIYUN_ACCESS_KEY_ID;
    const accessKeySecret = process.env.ALIYUN_ACCESS_KEY_SECRET;
    
    if (!accessKeyId || !accessKeySecret) {
      console.warn('[SMS:aliyun] 未配置阿里云 AK，降级到 mock 发送');
      return mockProvider.send(phone, code);
    }

    const client = new Core({
      accessKeyId,
      accessKeySecret,
      endpoint: 'https://dysmsapi.aliyuncs.com',
      apiVersion: '2017-05-25'
    });

    const params = {
      RegionId: "cn-hangzhou",
      PhoneNumbers: phone,
      SignName: process.env.ALIYUN_SMS_SIGN_NAME || '连山同城',
      TemplateCode: process.env.ALIYUN_SMS_TEMPLATE_CODE || 'SMS_000000',
      TemplateParam: JSON.stringify({ code })
    };

    try {
      const result: any = await client.request('SendSms', params, { method: 'POST' });
      if (result.Code !== 'OK') {
        throw new Error(result.Message);
      }
      return {};
    } catch (e: any) {
      console.error('[SMS:aliyun] 发送失败', e);
      throw new Error(`短信发送失败: ${e.message}`);
    }
  },
};

export function getSmsProvider(): SmsProvider {
  switch (env.smsProvider) {
    case 'aliyun':
    case 'tencent': // 演示: 暂未实现腾讯云，默认 fallback
      return aliyunProvider;
    default:
      return mockProvider;
  }
}
