# 连山壮瑶同城 (LianShan Local Services) - v2.0架构交接与二次开发准则

## 📌 项目状态与客观现实 (Project State & Objective Reality)
本项目是一款针对县域级下沉市场（目标覆盖人口 3~10 万，高并发支撑 3 万）量身打造的**本地同城电商交易与分类信息服务平台**。
历经多个版本的爆发式演进，项目现已全面升级至 **v2.0(商业种草生态闭环与沉浸式发布架构版)**。
我们在具备“淘宝级纯净购物车”与“3D粘土风全站UI”的基础上，彻底重构了“同城逛逛”的媒体内容生态。从底层数据库到前端路由，实现了“商业交易(Commerce)”与“生活动态(Moment)”链路的物理隔离，并打通了动态向商品导流的种草闭环。
现阶段系统极其稳定，且已成功部署至阿里云生产环境。客户端核心产物归档于 `D:\LsLife\releases\`。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. v2.0商业种草生态与双流发布引擎 (Seeding Ecosystem & Dual-Tab Publish)
*   **一源双端意图分流**：废弃了早期混乱的单表单发布模式。重构底层 `PublishBottomSheet`，采用 `HorizontalPager` 实现“商品/服务”与“同城逛逛”的双 Tab 物理隔离，根据不同意图精准派发不同路由。
*   **种草导流闭环算法**：底层 Prisma 模型新增 `linkedCommerceId` 与 `topic`，支持在非结构化的图文动态中，精准挂载属于该用户的 C2C/O2O 商品卡片。通过生活化内容实现交易降维打击。
*   **沉浸式图文发布器 (MomentPublishScreen)**：对标小红书等头部应用，将媒体选择区（`LazyRow`）置顶，辅以无边框大字号独立标题 (`20sp Bold`) 与自适应正文。底部挂载组件采用标准纵向 List，并根据路由下发的 `momentType` 状态机（如探店、需求），动态插入“星级评价”、“期望预算”等专属 UI。

### 2. V11.0 高精商业视觉与沉浸式体验 (Claymorphism UI & Immersive Layout)
*   **全局 3D 粘土风图标矩阵**：全站 10+ 个顶级大类已全面升级为具备高级光影质感的 3D 粘土风格（Claymorphism）。
*   **AI 端侧去背渲染管线 (U2-Net)**：所有 3D 图标均由服务器预执行 U2-Net 深度学习图像分割算法，彻底剥离底色生成无损 Alpha 通道 PNG (`rembg` 管线)。
*   **自适应 UI 缩放与 Insets 避让**：全站页面严格接入了 `.statusBarsPadding()` 及 `.imePadding()`，确保沉浸式状态栏不遮挡顶部元素，软键盘不遮挡底部输入框。

### 3. 淘宝级购物车与交易流转引擎 (Cart & Commerce)
*   **购物车防误触与多态拦截**：购物车支持“管理模式”（左上角动态计数，底部一键删除）。数据源实施严格的 `TradeMode` 拦截：O2O 商家渲染“店铺名”，C2C 闲置渲染“用户昵称”。

### 4. 深度即时通讯与交易存证体系 (Deep IM & Blockchain-Level Storage)
*   **图片极速流转算法 (`image`)**：采用“本地无损压缩 -> Base64 文本流 -> WebSocket 双工管道”传输，实现毫秒级图片上屏。
*   **原生语音通信 (`voice`)**：底层重写 `AudioManager` (AAC编码/m4a格式)，复刻“按住说话、松开发送/上划取消”原生级手势探测。
*   **区块链级防篡改存证**：后端 `hub.ts` 实施 **AES-256-CBC 对称加密** 落盘，利用 **SHA-256** 构建级联哈希不可篡改证据链。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend - `D:\LsLife\android\`)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34
*   **UI 框架**: Jetpack Compose / Material3 / 全局 `NavHost` 导航 (完美修复 Insets 冲突与路由乱码)
*   **架构 / 状态管理**: MVVM 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow
*   **网络通信**: Retrofit2 (RESTful) / OkHttp3 / WebSockets (全双工保活通信)

### 服务端与数据存储 (Backend - `D:\LsLife\backend\`)
*   **环境 / 框架**: Node.js / Express 兼容层 / TypeScript / PM2 热载托管 (`115.191.6.95`)
*   **数据库 / ORM**: PostgreSQL 关系型数据库 / Prisma ORM
*   **自动化脚本**: 包含 SSH 数据库热更新及图片批处理脚本。

---

## 🗄 数据库模型架构 (Database Schema - Prisma)
1. **`User`**: 核心主表（手机号极简注册、密码 Bcrypt 散列加密、爱心收藏夹映射）。
2. **`Post` / `Product`**: 双态商品/服务信息表。v2.0新增 `linkedCommerceId` 与 `topic` 字段用于支撑种草生态。
3. **`Favorite`**: 收藏表（多对多映射，用户点击红心后持久化存储关联商品 ID）。
4. **`Cart` / `CartEntry`**: 拓扑购物车结构。
5. **`Order` / `OrderItem`**: 订单流转核心表。
6. **`ChatSession` & `ChatMessage`**: 高并发交易沟通链路表。
7. **`Category` 树**: 高度封装的分类表，挂载透明 3D 高清图标（带 `?v=N` 缓存控制）。

---

## 🚀 自动化发版与路由规范 (Build & Routing Rules)
*   **发版归档**：执行 Release 时，项目根目录的 `version.properties` 会自动自增 VersionCode。当前版本从 **V2.01** 开始，以后版本的 `versionName` 每次严格叠加 **0.01**。
*   **编译指令**：`$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease` (仅允许将产物移动至 `D:\LsLife\releases\`)
*   **路由规范 (Navigation) 【强制】**：
    *   在 `NavHost` 中进行跨 tab 回退跳转时，务必使用 `popUpTo(navController.graph.findStartDestination().id)`。
    *   任何向 URL 传递的 String 路径参数，必须使用 `java.net.URLEncoder.encode(..., "UTF-8")` 封装，禁止裸露特殊字符（已彻底修复历史版本的解析乱码与崩溃问题）。

---

## 🎯 二次开发交接与下一阶段指引 (Next Stage Handover)

接手此项目进行**二次开发**的工程师/Agent，请基于极其成熟的 v2.0稳定盘，优先查阅以下待办清单：

1. **同城动态 Feeds 流卡片深度联排 (Moment Feed UI)**：
   种草数据的发布端已彻底完善（含图片、标题、正文、定位、关联商品等）。下一阶段需在 `CityStrollFeedScreen`（或首页瀑布流）中，将其渲染为对标小红书的“双列瀑布流图文卡片”，并透出所关联的 `linkedCommerceId` 商品卡片。
2. **全局高级搜索与多维过滤引擎 (Search & Filter Engine)**：
   数据底座已完全结构化。下一步需要基于 Prisma 复合查询，为搜索页开发“价格区间 + 分类标签 + 成色”等联合聚合查询功能。
3. **订单与履约生命周期闭环 (Order Lifecycle Management)**：
   购物车的结算链路（Checkout）已打通，当前急需完善商家端的“接单/核销”逻辑，以及买家端的订单状态流转 UI（待付款 -> 待发货 -> 待收货/自提 -> 已完成）。
4. **IM 地图与 O2O 闭环定位卡片 (LBS Integration)**：
   IM 底层协议已预留。需接入高德/腾讯地图 API，在聊天中实现 `type="location"` 的地图卡片互发，并引入基于经纬度的商品距离检索。
