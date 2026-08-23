# 连山同城 LsLife · 生产级后端

Node.js + TypeScript + Express + Prisma 实现的同城生活服务后端，覆盖鉴权、商家/商品、购物车、订单、支付、实时配送、同城发布、会员、通知与 AI 助手。

> **详细二次开发手册**（模块逻辑、API 契约、算法、扩展示例、部署）：[`docs/BACKEND_DEVELOPER_GUIDE.md`](../docs/BACKEND_DEVELOPER_GUIDE.md)

## 技术栈
- 运行时: Node.js 20+ / Express 4
- 数据: Prisma ORM（本地 SQLite，生产 PostgreSQL）+ Redis（缓存/限流，生产）
- 鉴权: 手机号短信验证码 + JWT
- 实时: WebSocket (`/ws`)
- 校验: Zod
- 支付/短信/AI/内容审核: 可插拔 Provider 抽象（`src/services/*`）

## 本地快速启动
```bash
cd backend
npm install                 # 若网络受限: npm install --registry=https://registry.npmmirror.com
cp .env.example .env
npx prisma generate
npx prisma db push          # 创建 SQLite 表
npm run seed                # 导入 7 个商家 / 14 个商品
npm run dev                 # http://localhost:4000/api
```

另开终端运行端到端冒烟测试（覆盖登录→下单→支付→追踪→发布→会员→AI）：
```bash
npm run smoke
```

## 目录结构
```
src/
  app.ts                # Express 装配
  index.ts              # HTTP + WebSocket 启动
  config/env.ts         # 环境变量
  lib/                  # prisma / jwt / http 响应 / 错误
  middleware/           # 鉴权 / 全局错误
  modules/              # auth merchants cart orders payments publish membership notifications addresses ai
  services/             # sms / payment / ai / moderation / delivery / order-fulfillment
  realtime/hub.ts       # WebSocket 推送
prisma/
  schema.prisma         # 数据模型
  seed.ts               # 迁移原型硬编码数据
scripts/smoke.ts        # 端到端冒烟
```

## 统一响应
```json
{ "code": 0, "message": "ok", "data": {} }
```
`code !== 0` 表示业务错误；HTTP 状态码同步反映。

## 核心接口
| 模块 | 方法 | 路径 |
| --- | --- | --- |
| 鉴权 | POST | /api/auth/send-code, /api/auth/login, /api/auth/realname; GET /api/auth/me |
| 商家 | GET | /api/merchants, /api/merchants/recommended, /api/merchants/:id |
| 购物车 | GET/POST/DELETE | /api/cart |
| 订单 | POST/GET | /api/orders, /api/orders/:id (含实时配送) |
| 支付 | POST | /api/payments/create, /api/payments/mock-confirm, /api/payments/callback/:provider |
| 发布 | POST/GET | /api/posts, /api/posts/quota |
| 会员 | GET/POST | /api/membership/plans, /api/membership/subscribe |
| 通知 | GET/POST | /api/notifications |
| 地址 | CRUD | /api/addresses |
| AI | POST | /api/ai/recommend |

## 生产化 TODO（需外部资质/密钥）
- 支付: 实现 `services/payment.ts` 微信支付 V3 / 支付宝下单与验签（需商户号、证书）。资金必须走持牌第三方。
- 短信: 实现 `services/sms.ts` 阿里云/腾讯云（需签名与模板审核）。
- AI: `AI_PROVIDER=dashscope` 并配置 `AI_API_KEY`（通义千问）。
- 数据库: `schema.prisma` provider 改 `postgresql`，`prisma migrate deploy`。
- 内容审核: `services/moderation.ts` 接入云内容安全 + 人审队列。
- 部署: 见 `Dockerfile` 与 `docker-compose.yml`。
