# 连山同城 LsLife · 后端开发详细手册

> 适用范围：`backend/` 目录  
> 技术栈：Node.js 20+ · TypeScript (ESM) · Express 4 · Prisma 5 · Zod · WebSocket (`ws`) · JWT  
> 数据库：本地 **SQLite** · 生产 **PostgreSQL 16**  
> 生产入口：`https://mentalhlp.site/api/` · `wss://mentalhlp.site/ws`

本文档面向后端二次开发：从目录、启动、数据模型、中间件、各业务模块、Provider 插件、实时通信、算法规则到扩展示例与部署，均按**当前仓库真实代码**编写。

---

## 目录

1. [快速上手](#1-快速上手)
2. [目录与分层](#2-目录与分层)
3. [启动与请求管线](#3-启动与请求管线)
4. [配置与环境变量](#4-配置与环境变量)
5. [数据库与 Prisma](#5-数据库与-prisma)
6. [公共库与中间件](#6-公共库与中间件)
7. [业务模块详解](#7-业务模块详解)
8. [服务层 Provider](#8-服务层-provider)
9. [WebSocket 实时层](#9-websocket-实时层)
10. [核心算法与状态机](#10-核心算法与状态机)
11. [完整 API 契约](#11-完整-api-契约)
12. [端到端主流程](#12-端到端主流程)
13. [二次开发实操](#13-二次开发实操)
14. [测试、部署与运维](#14-测试部署与运维)
15. [已知缺口](#15-已知缺口)

---

## 1. 快速上手

```bash
cd backend
npm install
# 网络受限时:
# npm install --registry=https://registry.npmmirror.com

cp .env.example .env
npx prisma generate
npx prisma db push      # 按 schema 建表 (本地 SQLite: prisma/dev.db)
npm run seed            # 导入演示商家/商品
npm run dev             # http://localhost:4000/api

# 另开终端
npm run smoke           # 端到端冒烟 (需先启动服务)
```

| npm script | 作用 |
|------------|------|
| `dev` | `tsx watch src/index.ts` 热重载 |
| `build` | `tsc` 输出到 `dist/` |
| `start` | `node dist/index.js` |
| `setup` | generate + db push + seed |
| `seed` | 写入演示数据 |
| `smoke` | 登录→下单→支付→发布→会员→AI |
| `lint` | `tsc --noEmit` |

---

## 2. 目录与分层

```
backend/
├── src/
│   ├── index.ts              # HTTP Server + 挂载 WebSocket
│   ├── app.ts                # Express 中间件与路由装配
│   ├── config/env.ts         # 环境变量集中读取
│   ├── lib/
│   │   ├── prisma.ts         # PrismaClient 单例
│   │   ├── jwt.ts            # 签发 / 校验 JWT
│   │   └── http.ts           # ok / fail / ApiError
│   ├── middleware/
│   │   ├── auth.ts           # requireAuth / optionalAuth
│   │   └── error.ts          # notFound / errorHandler / asyncHandler
│   ├── modules/              # REST 路由 (按域拆分)
│   │   ├── auth.ts
│   │   ├── merchants.ts
│   │   ├── cart.ts
│   │   ├── orders.ts
│   │   ├── payments.ts
│   │   ├── publish.ts
│   │   ├── membership.ts
│   │   ├── notifications.ts
│   │   ├── addresses.ts
│   │   └── ai.ts
│   ├── services/             # 可替换外部能力 + 履约逻辑
│   │   ├── sms.ts
│   │   ├── payment.ts
│   │   ├── ai.ts
│   │   ├── moderation.ts
│   │   ├── delivery.ts
│   │   └── order-fulfillment.ts
│   └── realtime/hub.ts       # WebSocket 连接表与推送
├── prisma/
│   ├── schema.prisma         # 数据模型
│   ├── seed.ts / seed-data.ts
│   └── migrations/           # (可选) 正式迁移
├── scripts/smoke.ts
├── deploy/                   # 生产脚本 (Nginx/PM2/PG)
├── docker-compose.yml
└── .env.example
```

### 分层约定

| 层 | 职责 | 禁止 |
|----|------|------|
| `modules/*` | HTTP 入参 Zod 校验、鉴权编排、调用 prisma/services、返回信封 | 直接调第三方 SDK、硬编码金额逻辑散落多处 |
| `services/*` | 短信/支付/AI/审核/配送模拟/履约事务 | 绑定 Express `req/res` |
| `lib/*` | 无业务的通用工具 | 引入业务表写死规则 |
| `realtime/*` | 连接管理与推送 | 复杂业务计算 |

金额、发布额度、库存/销量变更：**一律在服务端完成**，客户端只传 `productId` / `quantity` 等标识。

---

## 3. 启动与请求管线

### 3.1 进程启动

```
src/index.ts
  → createApp()                 # Express
  → http.createServer(app)
  → attachRealtime(server)      # path: /ws
  → server.listen(env.port)     # 默认 4000
```

HTTP 与 WebSocket **共用同一端口**；生产由 Nginx 分别反代 `/api` 与 `/ws`。

### 3.2 Express 中间件顺序 (`app.ts`)

```
helmet → cors → express.json(10mb)
  → GET /api/health
  → GET /api , /api/
  → /api/auth | merchants | cart | orders | payments
    | posts | membership | notifications | addresses | ai
  → notFound
  → errorHandler
```

### 3.3 统一响应信封

成功：

```json
{ "code": 0, "message": "ok", "data": { } }
```

失败（`ApiError` / Zod / 未捕获）：

```json
{ "code": 401, "message": "未登录或登录已过期", "data": null }
```

- `code === 0`：业务成功  
- `code !== 0`：业务或系统错误；HTTP status 通常与 `code` 对齐（如 400/401/403/404/429/500）  
- 客户端应以 `code` 为准，不要只看 HTTP 200

实现：`src/lib/http.ts`。

### 3.4 异步路由包装

所有 async 路由使用 `asyncHandler`，异常自动交给 `errorHandler`：

```ts
router.get('/', asyncHandler(async (req, res) => {
  // throw new ApiError(400, '...') 或 ZodError 均可
  return ok(res, data);
}));
```

---

## 4. 配置与环境变量

集中读取：`src/config/env.ts`。模板：`.env.example`。

| 变量 | 默认 | 说明 |
|------|------|------|
| `PORT` | `4000` | 监听端口 |
| `DATABASE_URL` | `file:./dev.db` | SQLite 或 PG 连接串 |
| `JWT_SECRET` | `dev-secret-change-me` | **生产必须换强随机** |
| `JWT_EXPIRES_IN` | `30d` | JWT 有效期 |
| `SMS_PROVIDER` | `mock` | `mock` / `aliyun` / `tencent` |
| `PAY_PROVIDER` | `mock` | `mock` / `wechat` / `alipay` |
| `AI_PROVIDER` | `mock` | `mock` / `dashscope` / `qianfan` / `doubao` |
| `AI_API_KEY` | 空 | 大模型密钥 |
| `AI_MODEL` | `qwen-plus` | 模型名 |
| `CONTENT_MODERATION_ENABLED` | `true` | 内容审核开关 |
| `NODE_ENV` | — | `production` 时减少 prisma 日志 |
| `WECHAT_*` / `ALIPAY_*` / `SMS_*` / `OSS_*` | — | 预留，代码侧多为 TODO |

冒烟测试可覆盖基址：`BASE=https://mentalhlp.site/api npm run smoke`。

---

## 5. 数据库与 Prisma

### 5.1 Provider 切换

`prisma/schema.prisma`：

```prisma
datasource db {
  provider = "sqlite"   // 生产改为 "postgresql"
  url      = env("DATABASE_URL")
}
```

| 环境 | provider | DATABASE_URL 示例 |
|------|----------|-------------------|
| 本地 | `sqlite` | `file:./dev.db` |
| 生产 | `postgresql` | `postgresql://user:pwd@127.0.0.1:5432/lslife?schema=public` |
| Docker Compose | `postgresql` | 见 `docker-compose.yml` 中 `api` 服务 |

切换到 PostgreSQL 后需：

1. 改 `provider`  
2. 改 `.env` 的 `DATABASE_URL`  
3. `npx prisma generate && npx prisma db push`（或 `migrate deploy`）  
4. `npm run seed`  

正式上线建议引入 `prisma migrate`，避免生产 `db push --accept-data-loss`。

### 5.2 模型一览

```mermaid
erDiagram
  User ||--o{ Address : has
  User ||--o{ CartItem : has
  User ||--o{ Order : places
  User ||--o{ Post : publishes
  User ||--o{ Notification : receives
  User ||--o{ Subscription : buys
  Merchant ||--o{ Product : sells
  Merchant ||--o{ Order : receives
  Product ||--o{ CartItem : in
  Product ||--o{ OrderItem : in
  Order ||--|{ OrderItem : contains
  Order ||--o| Payment : paid_by
  Order ||--o| Delivery : tracked_by
```

| 模型 | 用途与要点 |
|------|------------|
| `User` | 手机号唯一；`membershipTier`: free/vip/premium；身份证只存 `idCardHash` |
| `VerificationCode` | 登录验证码；5 分钟过期；`consumed` |
| `Address` | 收货地址；默认地址互斥 |
| `Merchant` | `externalId` 兼容种子 `m1`；`tags` 为 JSON 字符串 |
| `Product` | 归属商家；`stock`/`sales`；下单用库价 |
| `CartItem` | `@@unique([userId, productId])` |
| `Order` | 状态机见 §10；金额字段服务端写入 |
| `OrderItem` | 下单时快照名称/单价/图 |
| `Payment` | channel + `prepayPayload` JSON |
| `Delivery` | 骑手坐标与 progress |
| `Post` | UGC；`images` JSON；状态含 rejected |
| `Subscription` | 会员订购记录 |
| `Notification` | 履约通知等 |

### 5.3 Prisma 客户端

`src/lib/prisma.ts`：进程内单例 `PrismaClient`。业务代码一律：

```ts
import { prisma } from '../lib/prisma.js';
```

注意：本项目是 **ESM**（`package.json` 的 `"type": "module"`），本地 import 需带 `.js` 后缀（TS 源文件对应编译输出）。

### 5.4 种子数据

`npm run seed` 写入演示商家与商品（约 7 商家 / 14 商品）。`externalId` 形如 `m1`、`m1_1`，接口可用 cuid 或 externalId 查商家。

当前 seed 对已存在记录多为幂等 `upsert` 且 `update: {}`，**不会刷新**已有字段内容；改种子后需清库或改 update 逻辑。

---

## 6. 公共库与中间件

### 6.1 JWT (`lib/jwt.ts`)

载荷：

```ts
{ sub: userId, phone: string }
```

- `signToken`：使用 `JWT_SECRET` / `JWT_EXPIRES_IN`  
- `verifyToken`：校验失败抛错，由鉴权中间件转 401  

### 6.2 鉴权 (`middleware/auth.ts`)

| 中间件 | 行为 |
|--------|------|
| `requireAuth` | 必须 `Authorization: Bearer <token>`；写入 `req.userId` / `req.userPhone` |
| `optionalAuth` | 有合法 token 则解析，否则放行（如帖子列表 `mine=true`） |

扩展 Express：

```ts
req.userId?: string
req.userPhone?: string
```

### 6.3 错误处理 (`middleware/error.ts`)

| 异常类型 | HTTP | 响应 |
|----------|------|------|
| `ApiError` | `err.status` | `{ code: err.code, message }` |
| `ZodError` | 400 | 拼接 path + message |
| 其他 | 500 | `服务器内部错误`（控制台打 `[UnhandledError]`） |
| 未匹配路由 | 404 | `接口不存在` |

业务抛错示例：

```ts
throw new ApiError(403, '本月发布额度已用尽');
throw new ApiError(429, '验证码发送过于频繁, 请稍后再试');
```

---

## 7. 业务模块详解

以下路径均相对于 `/api`。

### 7.1 鉴权 · `modules/auth.ts`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/auth/register` | 无 | 手机号+密码注册 |
| POST | `/auth/login` | 无 | 手机号+密码登录 |
| POST | `/auth/change-password` | 强制 | 修改密码 |
| POST | `/auth/send-code` | 无 | 暂未开通短信（返回 503） |
| GET | `/auth/me` | 强制 | 当前用户（剔除 `idCardHash`） |
| POST | `/auth/realname` | 强制 | 实名；身份证 SHA-256 |
| PATCH | `/auth/profile` | 强制 | 昵称/头像 |

**发码逻辑**

1. 手机号 Zod：`/^1\d{10}$/`  
2. 60 秒内同号同 purpose 限频 → 429  
3. 生成 6 位数字码，入库 `expiresAt = now+5min`  
4. 调 `getSmsProvider().send`  
5. mock 时响应带回 `mockCode`（生产不应返回）

**登录逻辑**

1. 查最新未消费且未过期验证码，比对  
2. 标记 `consumed`  
3. 无用户则创建：`nickname = 连山用户{后四位}`  
4. 签发 JWT，返回 `{ token, user }`

**实名**：`idCard` 仅哈希入库，响应经 `sanitize` 去掉 `idCardHash`。

---

### 7.2 商家 · `modules/merchants.ts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/merchants` | 列表：category / q / sort / page / pageSize |
| GET | `/merchants/recommended` | rating Top 3 |
| GET | `/merchants/:id` | 详情；`id` 或 `externalId` |

查询参数：

- `sort`: `default` | `distance` | `sales` | `rating`  
- `page` 默认 1，`pageSize` 默认 20，最大 50  

`tags` 出库时 `JSON.parse`；响应中商品挂在 `items`（来自 `products` 关联）。

---

### 7.3 购物车 · `modules/cart.ts`（整模块 `requireAuth`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/cart` | 含 product（及 merchant） |
| POST | `/cart` | body: `{ productId, quantity }`；`quantity<=0` 删除 |
| DELETE | `/cart?merchantId=` | 清空某商家或全部 |

唯一键 upsert：`userId_productId`。

---

### 7.4 订单 · `modules/orders.ts`（整模块 `requireAuth`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/orders` | 创建订单（服务端计价） |
| GET | `/orders` | 当前用户订单列表 |
| GET | `/orders/:id` | 详情 + 实时配送 |
| POST | `/orders/:id/cancel` | 仅 `pending` 可取消 |

**创建订单 body**

```json
{
  "merchantId": "cuid 或 externalId",
  "items": [{ "productId": "...", "quantity": 2 }],
  "deliveryAddress": { "name": "...", "phone": "...", "address": "..." }
}
```

**计价**（防篡改）：

```
itemsTotal = Σ(库中单价 × quantity)
totalAmount = itemsTotal + merchant.deliveryFee
```

订单号：`LS` + 6 位数字（nanoid）。

详情接口对已支付订单调用 `computeLiveDelivery`，合并 `delivery` 字段返回。

---

### 7.5 支付 · `modules/payments.ts`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/payments/create` | 强制 | 统一下单 |
| POST | `/payments/mock-confirm` | 强制 | 演示确认支付 |
| POST | `/payments/callback/:provider` | 无 | 第三方异步回调 |

**create body**：`{ orderId, channel: wechat|alipay|wallet|mock }`

分支：

1. 订单必须存在且 `pending`  
2. `wallet`：校验余额 → 扣款 → `markOrderPaid` → `{ paid: true }`  
3. 其他 channel：写/更新 `Payment`，返回 `prepayPayload`  
4. 客户端 mock 流程再调 `mock-confirm` 传 `orderNo`

回调：`verifyCallback` 成功且订单仍 pending → `markOrderPaid`；响应 `{ code: 'SUCCESS' }`（简化版，真实微信/支付宝报文需按文档返回）。

---

### 7.6 履约 · `services/order-fulfillment.ts`

`markOrderPaid(orderId, transactionId)` 在 **事务**内：

1. Order → `paid`，写 `paidAt`  
2. Payment → `success`  
3. 各商品 `sales += quantity`  
4. upsert `Delivery`（演示骑手）  
5. 删除该用户该商家购物车  
6. 写 Notification（商家接单文案）  
7. 事务外：`pushToUser(userId, { event: 'order_paid', orderId, orderNo })`

这是支付成功后的**唯一履约入口**；新增支付渠道也应回调到此函数。

---

### 7.7 同城发布 · `modules/publish.ts`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/posts` | 强制 | 发布 |
| GET | `/posts` | 可选 | 信息流；`?category=&mine=` |
| GET | `/posts/quota` | 强制 | `{ used, limit, tier }` |

额度（自然月，`status !== rejected`）：

| tier | 每月上限 |
|------|----------|
| free | 3 |
| vip | 20 |
| premium | 50 |

发布前：`moderateContent(title, description)`；当前命中敏感词直接 `ApiError(400)`，通过则入库 `published`。

`images` 入库为 JSON 字符串，出库 parse。

---

### 7.8 会员 · `modules/membership.ts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/membership/plans` | 套餐列表（vip 9.9 / premium 19.9） |
| POST | `/membership/subscribe` | body `{ tier }`；**演示直开，不经支付** |

写入 `Subscription`，更新 `User.membershipTier` / `membershipUntil`（+1 月）。

生产改造：先创建支付单，回调成功后再改 tier。

---

### 7.9 通知 · `modules/notifications.ts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/notifications` | `{ list, unread }` |
| POST | `/notifications/read-all` | 全部已读 |
| POST | `/notifications/:id/read` | 单条已读 |
| DELETE | `/notifications` | 清空 |

---

### 7.10 地址 · `modules/addresses.ts`

标准 CRUD；设默认时先清其它地址的 `isDefault`；删除默认地址后自动把剩余第一条设为默认。

---

### 7.11 AI · `modules/ai.ts`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/recommend` | body `{ prompt }`；可选鉴权 |

返回 `{ reply, recommendations: [{ merchantId, itemId, name, price }] }`。

---

## 8. 服务层 Provider

所有外部能力通过工厂函数按 `env` 选择实现，便于二次开发时**只改服务层**。

### 8.1 短信 `services/sms.ts`

```ts
interface SmsProvider {
  send(phone: string, code: string): Promise<{ mockCode?: string }>;
}
```

| SMS_PROVIDER | 行为 |
|--------------|------|
| `mock`（默认） | 控制台打印；返回 `mockCode` |
| `aliyun` / `tencent` | 占位 TODO，需接云厂商 API |

### 8.2 支付 `services/payment.ts`

```ts
interface PaymentProvider {
  createPayment(input): Promise<{ prepayPayload; transactionId }>;
  verifyCallback(rawBody, headers): Promise<{ orderNo; success; transactionId }>;
}
```

| PAY_PROVIDER | 行为 |
|--------------|------|
| `mock` | 返回含 `confirmUrl` 的 prepay；回调直接 success |
| `wechat` / `alipay` | 抛「未配置」错误，需实现 V3/开放平台 |

合规约束：资金走持牌第三方，平台不做资金池。

### 8.3 AI `services/ai.ts`

| AI_PROVIDER | 行为 |
|-------------|------|
| `mock` | 基于库内商家拼推荐文案 |
| `dashscope` 等 | 调通义兼容接口；需 `AI_API_KEY`；要求模型返回 JSON |

### 8.4 内容审核 `services/moderation.ts`

本地敏感词：`黄赌毒`、`诈骗`、`暴力`、`违法`。  
关闭：`CONTENT_MODERATION_ENABLED=false`。  
生产应接云内容安全 + `pending_review` 人审队列。

### 8.5 配送模拟 `services/delivery.ts`

见 §10.2。生产应由骑手端上报 GPS，覆盖该函数。

---

## 9. WebSocket 实时层

文件：`src/realtime/hub.ts`。

| 项 | 值 |
|----|----|
| 路径 | `/ws?token=<JWT>` |
| 缺 token | close `4001` |
| token 无效 | close `4003` |
| 连接成功 | `{"event":"connected","userId":"..."}` |
| 服务端 API | `pushToUser(userId, payload)` |
| 已用事件 | `order_paid`：`{ event, orderId, orderNo }` |

内存结构：`Map<userId, Set<WebSocket>>`，支持同一用户多端。

**扩展建议**：骑手上报

```json
{ "event": "rider_location", "orderId": "...", "lat": 24.47, "lng": 112.08 }
```

服务端更新 `Delivery` 后 `pushToUser` 给下单用户。需按角色鉴权（当前仅用户 JWT）。

---

## 10. 核心算法与状态机

### 10.1 订单状态机

```
pending ──支付成功──► paid
   │                    │
   │ cancel             ▼
   ▼              preparing ──► delivering ──► delivered
cancelled
```

- 仅 `pending` 可取消  
- `preparing` / `delivering` / `delivered` 由配送模拟在查询时推进（并回写 DB）

### 10.2 配送进度算法（演示）

常量：备餐 20s + 配送 90s = 110s。  
用户演示坐标：`(24.472, 112.081)`。

| 条件 | status | progress | 坐标 |
|------|--------|----------|------|
| elapsed &lt; 20 | preparing | elapsed/20×100 | 商家点 |
| 20 ≤ elapsed &lt; 110 | delivering | (elapsed-20)/90×100 | 商家→用户线性插值 |
| elapsed ≥ 110 | delivered | 100 | 用户点 |

插值：

```
lat = merchantLat + (userLat - merchantLat) * ratio
lng = merchantLng + (userLng - merchantLng) * ratio
```

触发时机：`GET /orders/:id` 调用 `computeLiveDelivery`。

### 10.3 发布额度算法

```
monthStart = 本月1日 00:00:00
used = count(posts where userId && createdAt >= monthStart && status != rejected)
limit = { free:3, vip:20, premium:50 }[tier]
if used >= limit → 403
```

### 10.4 订单号

`LS` + `customAlphabet('0123456789', 6)`。

---

## 11. 完整 API 契约

### 11.1 通用头

```
Content-Type: application/json
Authorization: Bearer <JWT>   # 需鉴权接口
```

### 11.2 速查表

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/health` | 无 |
| GET | `/api/` | 无 |
| POST | `/api/auth/send-code` | 无 |
| POST | `/api/auth/login` | 无 |
| GET | `/api/auth/me` | 强制 |
| POST | `/api/auth/realname` | 强制 |
| PATCH | `/api/auth/profile` | 强制 |
| GET | `/api/merchants` | 无 |
| GET | `/api/merchants/recommended` | 无 |
| GET | `/api/merchants/:id` | 无 |
| GET/POST/DELETE | `/api/cart` | 强制 |
| POST/GET | `/api/orders` | 强制 |
| GET | `/api/orders/:id` | 强制 |
| POST | `/api/orders/:id/cancel` | 强制 |
| POST | `/api/payments/create` | 强制 |
| POST | `/api/payments/mock-confirm` | 强制 |
| POST | `/api/payments/callback/:provider` | 无 |
| POST/GET | `/api/posts` | POST 强制 / GET 可选 |
| GET | `/api/posts/quota` | 强制 |
| GET | `/api/membership/plans` | 无 |
| POST | `/api/membership/subscribe` | 强制 |
| GET/POST/DELETE | `/api/notifications` … | 强制 |
| GET/POST/PUT/DELETE | `/api/addresses` … | 强制 |
| POST | `/api/ai/recommend` | 可选 |

### 11.3 联调示例（mock 登录）

```bash
# 发码
curl -s -X POST http://localhost:4000/api/auth/send-code \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800138000"}'
# 取 data.mockCode 登录

curl -s -X POST http://localhost:4000/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800138000","code":"123456"}'
```

---

## 12. 端到端主流程

```mermaid
sequenceDiagram
  participant C as Client
  participant A as Auth
  participant M as Merchants/Cart
  participant O as Orders
  participant P as Payments
  participant F as markOrderPaid
  participant WS as WebSocket
  participant D as Delivery

  C->>A: send-code / login
  A-->>C: JWT
  C->>M: 浏览商家 / 加购
  C->>O: POST /orders (仅 id+数量)
  O-->>C: pending 订单 (服务端金额)
  C->>P: create + mock-confirm
  P->>F: markOrderPaid
  F->>WS: order_paid
  C->>O: GET /orders/:id
  O->>D: computeLiveDelivery
  O-->>C: status + rider 坐标
```

冒烟脚本 `scripts/smoke.ts` 覆盖：健康检查 → 登录 → 实名 → 商家 → 购物车 → 地址 → 下单计价断言 → mock 支付 → 追踪 → 发布额度 → 会员提升额度 → AI。

---

## 13. 二次开发实操

### 13.1 新增一个 REST 模块

1. **（可选）** 改 `prisma/schema.prisma` → `npx prisma db push`  
2. 新建 `src/modules/xxx.ts`：

```ts
import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

router.get(
  '/',
  requireAuth,
  asyncHandler(async (req, res) => {
    // ...
    return ok(res, data);
  }),
);

export default router;
```

3. 在 `app.ts`：`app.use('/api/xxx', xxxRoutes);`  
4. 在 `scripts/smoke.ts` 加断言  
5. 更新本文档 API 表  

### 13.2 接入真实短信

1. 实现 `services/sms.ts` 中 `aliyunProvider.send`（签名、模板、AccessKey）  
2. `.env`：`SMS_PROVIDER=aliyun` 及 `SMS_*`  
3. **去掉**生产响应对 `mockCode` 的依赖（可仅 `NODE_ENV!==production` 时返回）  
4. 保留 60s 限频与验证码过期逻辑  

### 13.3 接入微信支付

1. 实现 `wechatProvider.createPayment`：调用微信统一下单，返回 APP 调起参数  
2. 实现 `verifyCallback`：验签、解析 `orderNo` / 流水号  
3. 验签成功后调用 `markOrderPaid`（注意幂等：订单已非 pending 则跳过）  
4. `PAY_PROVIDER=wechat`，配置 `WECHAT_*` 与公网 `NOTIFY_URL`  
5. 删除或限制生产环境的 `/mock-confirm`  

### 13.4 会员改为先付后开

1. `subscribe` 只创建「待支付订阅单」或复用 `Payment`  
2. 支付回调成功后再写 `Subscription` + 更新 `membershipTier`  
3. 禁止无支付直开  

### 13.5 真实配送

1. 新增鉴权角色或骑手 token  
2. WS 或 REST 接收 GPS → 更新 `Delivery`  
3. `pushToUser` 推送 `rider_location`  
4. `computeLiveDelivery` 改为读库真实坐标，去掉时间插值  

### 13.6 切换 PostgreSQL（生产）

参考 `deploy/migrate-to-postgres.sh`：

1. schema `provider = "postgresql"`  
2. 创建库与用户  
3. `prisma generate && prisma db push`（或 migrate）  
4. seed → build → PM2 restart  

### 13.7 编码规范（后端）

- 使用 `asyncHandler` + `ApiError`，不要手写 `try/catch` 漏调 next  
- Zod 解析 `req.body` / `req.query`（query 注意 `z.coerce`）  
- 金额、额度、库存只信数据库与服务端计算  
- Provider 用接口 + 工厂，模块不直接 `if (env.xxx)` 散落  
- ESM import 带 `.js` 后缀  

---

## 14. 测试、部署与运维

### 14.1 本地测试

```bash
npm run lint
npm run smoke
# 生产冒烟
BASE=https://mentalhlp.site/api npm run smoke
```

### 14.2 Docker Compose（联调）

`docker-compose.yml` 提供 `postgres:16` + `redis:7` + `api`。  
使用前需将 Prisma provider 改为 `postgresql`。  
说明：Redis 在 compose 中预留，**业务代码尚未接入**。

### 14.3 生产部署要点

| 组件 | 说明 |
|------|------|
| 进程 | PM2（`lslife-api`）+ systemd 保活 |
| 反代 | Nginx：`/api` → Node；`/ws` Upgrade |
| TLS | Let's Encrypt |
| DB | PostgreSQL 16 |
| 脚本 | `backend/deploy/setup-production.sh` 等 |

常用：

```bash
pm2 status
pm2 logs lslife-api
pm2 restart lslife-api
sudo nginx -t && sudo systemctl reload nginx
```

### 14.4 安全清单（上线前）

- [ ] 更换强 `JWT_SECRET`  
- [ ] 关闭或保护 `mock-confirm`  
- [ ] 短信不回传验证码  
- [ ] 支付回调验签 + 金额校验  
- [ ] HTTPS only；安全组仅 80/443  
- [ ] 内容审核接云服务  
- [ ] 数据库定期备份  

合规详见 [`COMPLIANCE.md`](COMPLIANCE.md)。

---

## 15. 已知缺口

| 项 | 现状 |
|----|------|
| 微信/支付宝 | Provider 抛错占位 |
| 短信云厂商 | TODO 占位 |
| 会员订阅 | 演示直开，无支付 |
| 内容审核 | 本地词表 |
| Redis / OSS | 环境与 compose 有规划，代码未用 |
| Prisma migrate | 多用 `db push` |
| 配送 | 时间驱动模拟，非真实 GPS |
| 库存扣减 | 履约只加销量，未严格扣 `stock` |

---

## 相关文档

| 文档 | 内容 |
|------|------|
| [`backend/README.md`](../backend/README.md) | 后端快速开始 |
| [`DEVELOPER_HANDBOOK.md`](DEVELOPER_HANDBOOK.md) | 全栈开发者手册 |
| [`COMPLIANCE.md`](COMPLIANCE.md) | 商业化合规 |
| [`android/README.md`](../android/README.md) | 客户端对接 |

---

**维护约定**：变更路由、模型或履约逻辑时，同步更新本文 §7 / §10 / §11，并补充 `smoke.ts` 断言。
