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

// 真实场景: 接入微信支付 V3 / 支付宝 SDK
const wechatProvider: PaymentProvider = {
  async createPayment(input) {
    console.log('[Pay:wechat] 微信统一下单', input.orderNo);
    // 模拟微信统一下单返回
    return {
      transactionId: `WX${Date.now()}`,
      prepayPayload: {
        appId: 'wx123456',
        timeStamp: String(Math.floor(Date.now() / 1000)),
        nonceStr: 'random',
        package: 'prepay_id=wx123',
        signType: 'RSA',
        paySign: 'mock_sign'
      },
    };
  },
  async verifyCallback(rawBody, headers) {
    // 真实场景：
    // 1. 获取 headers['wechatpay-signature']
    // 2. 构造验签 payload
    // 3. 使用微信公钥验证
    console.log('[Pay:wechat] 验证回调签名', headers);
    const body = rawBody as { resource?: { ciphertext: string }; out_trade_no?: string };
    
    // 防重放与伪造校验模拟
    const isValidSignature = true;
    if (!isValidSignature) {
       return { orderNo: '', success: false, transactionId: '' };
    }

    // 假设解密后得到 orderNo
    const orderNo = body.out_trade_no ?? `MOCK_${Date.now()}`;
    return {
      orderNo,
      success: true,
      transactionId: `WX_TRANS_${Date.now()}`
    };
  },
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
