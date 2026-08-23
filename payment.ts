import { env } from '../config/env.js';

export type PayChannel = 'wechat' | 'alipay' | 'wallet' | 'mock';

export interface CreatePaymentInput {
  orderNo: string;
  amount: number;
  channel: PayChannel;
  description: string;
}

export interface CreatePaymentResult {
  /** 客户端拉起支付所需参数 (原生 SDK 使用) */
  prepayPayload: Record<string, unknown>;
  transactionId: string;
}

/**
 * 支付服务抽象层。
 * 关键商业化约束: 资金必须走持牌第三方 (微信支付/支付宝), 平台不得自建资金池。
 * 流程: 服务端统一下单 -> 返回 prepay 参数 -> 客户端原生 SDK 拉起 -> 第三方异步回调 -> 服务端验签并置订单为已支付 -> 对账。
 */
export interface PaymentProvider {
  createPayment(input: CreatePaymentInput): Promise<CreatePaymentResult>;
  /** 验签异步回调, 返回 { orderNo, success, transactionId } */
  verifyCallback(rawBody: unknown, headers: Record<string, unknown>): Promise<{ orderNo: string; success: boolean; transactionId: string }>;
}

const mockProvider: PaymentProvider = {
  async createPayment(input) {
    const transactionId = `MOCK${Date.now()}${Math.floor(Math.random() * 1000)}`;
    return {
      transactionId,
      prepayPayload: {
        channel: input.channel,
        mock: true,
        // 演示: 客户端可直接调用 /api/payments/mock-confirm 完成"支付"
        confirmUrl: `/api/payments/mock-confirm`,
        orderNo: input.orderNo,
        amount: input.amount,
      },
    };
  },
  async verifyCallback(rawBody) {
    const body = rawBody as { orderNo: string; transactionId?: string };
    return {
      orderNo: body.orderNo,
      success: true,
      transactionId: body.transactionId ?? `MOCK${Date.now()}`,
    };
  },
};

import WxPay from 'wechatpay-node-v3';
import crypto from 'crypto';

// 微信支付 (APP 下单 + V3 验签)
const wechatProvider: PaymentProvider = {
  async createPayment(input) {
    if (!process.env.WECHAT_MCH_ID) {
      console.warn('[Pay:wechat] WECHAT_MCH_ID not configured, falling back to mock provider');
      return mockProvider.createPayment(input);
    }
    const pay = new WxPay({
      appid: process.env.WECHAT_APP_ID || '',
      mchid: process.env.WECHAT_MCH_ID || '',
      publicKey: process.env.WECHAT_PUBLIC_KEY ? Buffer.from(process.env.WECHAT_PUBLIC_KEY, 'utf8') : Buffer.from(''),
      privateKey: process.env.WECHAT_PRIVATE_KEY ? Buffer.from(process.env.WECHAT_PRIVATE_KEY, 'utf8') : Buffer.from(''),
    });

    const params = {
      description: input.description,
      out_trade_no: input.orderNo,
      notify_url: process.env.WECHAT_NOTIFY_URL || 'https://mentalhlp.site/api/payments/callback/wechat',
      amount: {
        total: Math.round(input.amount * 100), // convert to cents
      },
    };

    const result = await pay.transactions_app(params);
    if (result.status === 200 && result.data && result.data.prepay_id) {
        const prepayId = result.data.prepay_id;
        const timestamp = Math.floor(Date.now() / 1000).toString();
        const nonceStr = crypto.randomBytes(16).toString('hex');
        
        const message = `${process.env.WECHAT_APP_ID}\n${timestamp}\n${nonceStr}\n${prepayId}\n`;
        const sign = crypto.createSign('SHA256').update(message).sign(process.env.WECHAT_PRIVATE_KEY || '', 'base64');
        
        return {
          transactionId: '',
          prepayPayload: {
            appid: process.env.WECHAT_APP_ID,
            partnerid: process.env.WECHAT_MCH_ID,
            prepayid: prepayId,
            package: 'Sign=WXPay',
            noncestr: nonceStr,
            timestamp: timestamp,
            sign: sign
          }
        };
    } else {
      throw new Error('微信统一下单失败: ' + JSON.stringify(result.data));
    }
  },
  async verifyCallback(rawBody, headers) {
    if (!process.env.WECHAT_MCH_ID) {
      return mockProvider.verifyCallback(rawBody, headers);
    }
    const pay = new WxPay({
      appid: process.env.WECHAT_APP_ID || '',
      mchid: process.env.WECHAT_MCH_ID || '',
      publicKey: process.env.WECHAT_PUBLIC_KEY ? Buffer.from(process.env.WECHAT_PUBLIC_KEY, 'utf8') : Buffer.from(''),
      privateKey: process.env.WECHAT_PRIVATE_KEY ? Buffer.from(process.env.WECHAT_PRIVATE_KEY, 'utf8') : Buffer.from(''),
    });
    
    try {
      const body = typeof rawBody === 'string' ? JSON.parse(rawBody) : (rawBody as any);
      const resource = body?.resource || {};
      
      const decryptedStr = pay.decipher_gcm(
        resource.ciphertext || '',
        resource.associated_data || '',
        resource.nonce || '',
        process.env.WECHAT_API_V3_KEY || ''
      );
      
      const decrypted = JSON.parse(decryptedStr as string) as any;
      return {
        orderNo: decrypted.out_trade_no as string,
        success: decrypted.trade_state === 'SUCCESS',
        transactionId: decrypted.transaction_id as string,
      };
    } catch (e) {
      console.error('WeChat callback error', e);
      return { orderNo: '', success: false, transactionId: '' };
    }
  }
};

export function getPaymentProvider(): PaymentProvider {
  switch (env.payProvider) {
    case 'wechat':
    case 'alipay':
      return wechatProvider;
    default:
      return mockProvider;
  }
}
