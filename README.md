# 连山同城 LsLife

连山壮族瑶族自治县同城生活服务平台。本仓库包含三部分：

| 目录 | 说明 | 状态 |
| --- | --- | --- |
| [`android/`](android/README.md) | 原生 Android 客户端（Kotlin + Jetpack Compose） | 商业化重构主体 |
| [`backend/`](backend/README.md) | 生产级后端（Node + TypeScript + Prisma） | 可运行 + 端到端冒烟通过 |
| `src/` (原 `server.ts`) | 早期 React 网页高保真原型 | 作为设计/交互参考 |
| [`docs/COMPLIANCE.md`](docs/COMPLIANCE.md) | 商业化合规与上线清单 | 上线前对照执行 |
| [`docs/DEVELOPER_HANDBOOK.md`](docs/DEVELOPER_HANDBOOK.md) | 开发者手册（架构/协议/二次开发） | 二次开发必读 |
| [`docs/BACKEND_DEVELOPER_GUIDE.md`](docs/BACKEND_DEVELOPER_GUIDE.md) | 后端开发详细手册 | 后端二次开发必读 |
| [`docs/FULL_STACK_ANALYSIS_2026-07-20.md`](docs/FULL_STACK_ANALYSIS_2026-07-20.md) | Antigravity 升级后全栈分析（含生产 SSH/DB） | 当前基线评估 |

## 架构概览

```
Android App (Compose/MVVM/Hilt)
   │  Retrofit/OkHttp (HTTPS)  +  WebSocket (实时配送)
   ▼
后端 API (Express + Prisma)
   ├─ 鉴权(短信+JWT) / 实名
   ├─ 商家·商品 / 购物车 / 订单 / 支付(持牌第三方) / 对账
   ├─ 同城发布(内容审核) / 会员 / 通知
   ├─ 实时配送(WebSocket) / AI 助手(境内合规大模型)
   └─ PostgreSQL + Redis + 对象存储 (生产)
```

## 生产环境

- API: `https://mentalhlp.site/api/`
- WebSocket: `wss://mentalhlp.site/ws`
- 健康检查: `https://mentalhlp.site/api/health`
- 服务器部署脚本: `backend/deploy/`（Nginx + HTTPS + PostgreSQL）

## 快速开始（本地）

1. 启动后端并跑通冒烟测试：见 [`backend/README.md`](backend/README.md)。
2. 用 Android Studio（JDK 17）打开 `android/` 构建运行：见 [`android/README.md`](android/README.md)。

## 重构要点（相较原型）
- 从网页原型 → 原生 Android，从内存假数据 → 数据库持久化。
- 模拟支付 → 服务端统一下单 + 第三方回调对账抽象。
- 手绘 SVG 地图/伪配送 → 实时配送数据模型 + WebSocket 推送（地图 SDK 待接入密钥）。
- 无鉴权 → 手机号验证码登录 + JWT + 实名。
- 前端 `useState` 会员额度 → 服务端强制校验发布额度与会员。
- Gemini → 境内合规大模型（通义/文心/豆包）可插拔 Provider。

> 带外部资质/密钥的项（支付、地图、短信、推送、上架）见 `docs/COMPLIANCE.md`，代码已预留接入点。
