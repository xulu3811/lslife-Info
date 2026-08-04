# 连山壮瑶同城 (LianShan Local Services) - V7.0 架构交接与二次开发准则

## 📌 项目状态与客观现实 (Project State & Objective Reality)
本项目是一款针对县域级下沉市场（目标覆盖人口 3~10 万，高并发支撑 3 万）量身打造的**本地同城电商交易与分类信息服务平台**。
历经多个版本的爆发式演进，项目现已全面升级至 **V6.5 (动态交易与信息发布解耦版)**。
我们在具备“淘宝级纯净购物车体验 + 闲鱼动态发布”底层逻辑的同时，打通了极度成熟的多模态即时通讯体系，并彻底完成了前端 UI 体验的脱胎换骨。
现阶段系统非常稳定，且已成功部署至阿里云生产环境。客户端核心产物归档于 `D:\LsLife\releases\`。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. V6.5 淘宝级购物车与交易流转引擎 (Cart & Commerce)
*   **淘宝级视觉重构**：全面摒弃早期的简陋列表与左滑删除。购物车采用浅灰蓝 (`#F5F6F8`) 沉浸式底色，所有商铺分组被重构为拥有 16dp 完美大圆角的**纯白悬浮卡片**。
*   **购物车“管理模式”防误触算法**：左上角购物车标题动态显示商品数量 `(N)`；右上角新增“管理”入口。进入管理模式后，底部“去结算”按钮动态切换为红色“删除”按钮，支持跨店批量勾选、一键删除。
*   **多态店铺分类隔离机制**：购物车数据源已进行严格的 `TradeMode` 拦截（仅允许 `COMMERCE` 交易类进入）。UI 层基于 `merchantId/sellerId` 动态渲染分组头部：
    *   **O2O 商家**：渲染 `[店铺 Icon] + [店铺名]`
    *   **C2C 个人闲置**：渲染 `[真实圆角头像] + [用户昵称]`
*   **防误触整行跳转**：商品卡片整行点击已绑定至 `navController.navigate(Routes.POST_DETAIL)` 无缝跳转商品详情，单选/反选操作被严格收敛至左侧圆形 Checkbox，彻底对齐业界顶级电商体验。

### 2. 双核动态发布引擎 (Dynamic Publish Engine)
*   **多态路由驱动 (`TradeMode`)**：系统将原本的混编流拆分为双核。发布信息时，根据 `CategoryConfig` 映射的 `TradeMode` (`INFO_PUBLISH` vs `COMMERCE`)，决定前端展现哪些属性字段，并在商品详情页呈现截然不同的底部导航栏。
*   **NLP 语义级 AI 提取**：针对非结构化长文本（二手房、二手车等），前端接入大模型 NLP 接口，一键智能提炼品牌、成色、价格等核心要素并自动回填。

### 3. 深度即时通讯与交易存证体系 (Deep IM & Blockchain-Level Storage)
*   **图片极速流转算法 (`image`)**：突破传统 HTTP 瓶颈，采用“本地无损压缩 -> Base64 文本流 -> WebSocket 双工管道”直连服务端的传输算法。客户端拦截字节流直接喂给 Coil 渲染引擎，实现毫秒级图片上屏。
*   **原生语音通信 (`voice`)**：底层重写 `AudioManager`，采用高压高音质 **AAC编码/m4a格式**，结合 Compose `pointerInput` 手势探测，完美复刻国民级“按住说话、松开发送/上划取消”操作。
*   **区块链级防篡改存证**：后端 `hub.ts` 实施 **AES-256-CBC 对称加密** 落盘，且每条消息均基于“前一条哈希 + 当前明文 + 特征”计算 **SHA-256** 哈希，构成不可篡改的证据链。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend - `D:\LsLife\android\`)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34
*   **UI 框架**: Jetpack Compose / Material3 / 全局 `NavHost` 导航 (已修复 `popUpTo` 单例栈冲突)
*   **架构 / 状态管理**: MVVM 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow
*   **网络通信**: Retrofit2 (RESTful) / OkHttp3 / WebSockets (全双工保活通信)

### 服务端与数据存储 (Backend - `D:\LsLife\backend\`)
*   **环境 / 框架**: Node.js / Express 兼容层 / TypeScript / PM2 热载托管 (`115.191.6.95`)
*   **数据库 / ORM**: PostgreSQL 关系型数据库 / Prisma ORM
*   **加密安防**: Node.js 原生 Crypto 模块 (AES-256 + SHA-256)

---

## 🗄 数据库模型架构 (Database Schema - Prisma)
1. **`User`**: 核心主表（手机号极简注册、密码 Bcrypt 散列加密、实名与会员权益状态）。
2. **`Post` / `Product`**: 双态商品/服务信息表（核心字段：`tradeMode` 标识业务形态、JSON格式的 `attributes` 支撑灵活多态扩容）。
3. **`Cart` / `CartEntry`**: 拓扑购物车结构。强关联 `merchantId` 和 `sellerId` 区分 O2O 与 C2C 逻辑。
4. **`Order` / `OrderItem`**: 订单流转核心表（关联收货地址、支付状态、履约生命周期）。
5. **`ChatSession` & `ChatMessage`**: 高并发交易沟通链路表（SHA-256 哈希指针级联）。
6. **`Category` 树**: 高度封装的分类表，已内置 `iconUrl` 挂载 3D 高清图标矩阵。

---

## 🚀 自动化发版与路由规范 (Build & Routing Rules)
*   **发版归档**：执行 Release 时，项目根目录的 `version.properties` 会自动自增 VersionCode。
*   **编译指令**：`$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease` (仅允许将产物移动至 `D:\LsLife\releases\`)
*   **路由规范 (Navigation) 【强制】**：
    *   在 Jetpack Compose `NavHost` 中进行跨 tab 回退跳转时，务必使用 `popUpTo(navController.graph.findStartDestination().id)`，严禁直接 `popUpTo(String)` 以防止后退栈死锁。
    *   任何向 URL 传递的 String 路径参数，必须使用 `java.net.URLEncoder.encode(..., "UTF-8")` 进行封装。

---

## 🎯 二次开发交接与下一阶段指引 (Next Stage Handover)

接手此项目进行**二次开发**的工程师/Agent，请优先查阅以下未尽事宜，并基于此开启新工作：

1. **全局高级搜索与多维过滤引擎 (Search & Filter Engine)**：
   目前数据底座已完全结构化。下一步需要基于 Prisma 的复合查询，为搜索页开发“价格区间 + 分类标签 + 成色”等联合聚合查询功能。
2. **订单与履约生命周期闭环 (Order Lifecycle Management)**：
   购物车的结算链路（Checkout）已打通，当前急需完善商家端的“接单/核销”逻辑，以及买家端的订单状态流转 UI（待付款 -> 待发货 -> 待收货/自提 -> 已完成）。
3. **IM 地图与 O2O 闭环定位卡片 (LBS Integration)**：
   IM 底层协议已预留。需接入高德/腾讯地图 API，在聊天中实现 `type="location"` 的地图卡片互发，并引入基于经纬度 (`GeoJSON`) 的商品距离检索。
