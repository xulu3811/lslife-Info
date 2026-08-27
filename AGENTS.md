# 同城•连山 (LianShan Local Services) - V7.24 深度交接与架构总结文档

## 📌 项目定位与当前进度 (Project State & Objective Reality)
本项目是一款针对县域级市场（目标覆盖人口 3~10 万，高并发支撑 3 万）量身打造的**本地同城分类信息与生活服务平台**。

历经多个版本的演进，目前项目已全面跨越至 **V7.24 (极简美学 UI 统一与微交互版)**。
在具备区块链防篡改存证与底层动态表单数据流的基底上，最新版本在视觉体验上实现了商业级对齐：
1. **全局 UI 缩放与比例统一**：深度重构了“首页”与“分类”的组件尺寸。统一采用了 `48.dp` 的金刚区核心图标基准，分类页升级为 3 列垂直图文卡片布局。
2. **瀑布流 (Feed) 视觉降压**：强制瀑布流卡片图片的最大比例为 `3:2` (AspectRatio 1.5f)，彻底消灭了长图抢占视觉重心的乱象；对用户头像 (20.dp)、联系按钮 (24.dp)、字体层级 (13.sp/11.sp) 与内边距 (8.dp) 进行了微缩处理。
3. **高定微交互动画 (Micro-interactions)**：为首页顶部 TabRow 引入了平滑的字号与灰度渐变，并为“同城动态”设计了克制且吸睛的 `1.2s` 红点呼吸渐变动画 (Breathing Badge)。
4. **底层构建优化**：为了对抗繁重的 R8 混淆压缩内存溢出，编译链路已规范化引入了 `--no-daemon` 安全策略。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. 统一合规审批流与发布拦截 (Unified Approval & Risk Control)
*   **强鉴权拦截**：在 Node.js 后端的 `publish.ts` 中结合 `requireQuota` 中间件，严格要求用户必须是 `realNameStatus === 'verified'` 或 `isMerchant === true` 方可发帖，并特别保留了 `13828577665` 等超级管理员的无视限制穿透 (Bypass) 权限。
*   **状态机流转**：系统通过 `AI_REVIEWING` -> `MANUAL_REVIEWING` -> `PUBLISHED` 的状态机进行内容风控管控。

### 2. 千人千面：基于 JSONB 的动态属性表单 (Dynamic Attributes Schema)
*   **非结构化存储**：底层 PostgreSQL 采用 `attributes: Json?` 字段，实现免 Schema 变更的无限扩展。
*   **动态渲染**：客户端利用 `CategorySchemaRegistry` 解析动态规则，在 Compose UI 中将其渲染成高级数据表格，支持多选高亮标签等复杂 UI。

### 3. AI 视觉引擎与 3D Soft UI (Aesthetics & AI Processing)
*   **智能包围盒裁剪管线 (AI Icon Processing)**：摒弃传统扁平 SVG，利用 Python (`rembg` + `Pillow`) 对实物网图进行去背、极限裁剪并统一输出为 512x512 包含 15% 留白呼吸感的 RGBA 透明底 PNG。
*   **3D Soft UI**：严格统一使用 `12.dp` 圆角、纯白背景卡片，以及带有 `Color(0x1A000000)` 的超细高亮阴影，搭配微边框设计。

### 4. 深度即时通讯与区块链级交易存证 (Deep IM & Blockchain Storage)
*   **通信协议**：基于 WebSocket 的直连即时通讯，配合本地无损图片压缩与 Base64 文本流极速传输。
*   **区块链级存证**：后端在落盘时执行 **AES-256-CBC** 对称加密，并利用上一条消息的哈希值计算 **SHA-256 级联哈希**，构建不可篡改的消息证据链。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34 / JDK 17
*   **UI 框架**: Jetpack Compose / Material3 / 全局 NavHost 路由
*   **架构**: MVVM / 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow / Coil
*   **网络与解析**: Retrofit2 / OkHttp3 / WebSockets / Kotlinx Serialization

### 服务端与数据存储 (Backend)
*   **环境 / 框架**: Node.js / Express / TypeScript / PM2 热载托管
*   **数据库 / ORM**: PostgreSQL / Prisma ORM
*   **自动化热更新**: 提供 `deploy_clean.mjs` (Node-SSH 脚本)，实现一键清空远端缓存、编译代码、强制同步 Prisma DB 结构并重启 PM2 守护进程。

---

## 🗄 服务器与数据库基础设施 (Infrastructure)

1. **服务器信息**:
   * **系统**: Ubuntu 24.04 | **IP**: `115.191.6.95` | **域名**: `mentalhlp.site`
   * **日常账号**: `lslife` (公钥免密) | **Root账号**: `root` (密码：`Maxence2468;`)
2. **数据库信息 (PostgreSQL - Docker)**:
   * **端口**: 宿主机 `5433` -> 容器 `5432`
   * **数据库名**: `lslife` | **用户名**: `lslife` | **密码**: `af4a98b163543c58c46bf827bdd546a8`
3. **API 资源密钥**:
   * **DeepSeek API Key**: `sk-30f79d21acbd487da71ec3cb5ce63d54`

---

## 🛑 平台红线与开发原则 (Core Platform Rules)

> **以下为连山同城 V7.x 后期的绝对红线，任何 Agent 或开发者不得违背：**

1. **纯信息发布平台，严禁电商闭环**：
   - 本项目定位于纯信息发布平台，绝对**不提供**在线电商交易闭环。
   - 严禁引入或使用任何“购物车 (CartItem)”、“订单 (Order)”、“在线支付流水 (Payment)”、“物流发货 (Delivery)”逻辑与表结构。
   - 任何涉及资金流动的仅限平台自身服务（例如：购买发帖配额、置顶帖子、商家入驻认证），不包含物理商品的 C2C/B2C 交易。
2. **轻量化位置体系，废弃 LBS 地图 SDK**：
   - 客户端严禁引入高德、腾讯等重型 3D/2D 地图 SDK，因为这将导致包体积暴增且增加维护成本。
   - 位置信息不使用基于 `latitude` / `longitude` 的 Haversine 球面距离计算。
   - 全面转为轻量级文本层级方案：要求信息发布者手动选择或填写“**省-市-县-镇（区）+ 详细街道/店铺地址**”。数据查询通过行政区划名称的 `Prisma.sql` 文本过滤来实现“同城”或“附近”的划分。

---

## 🚀 自动化编译与发版指引 (Build Rules)

**客户端安全编译指令 (推荐在 PowerShell 中执行)**：
为了防止内存溢出和文件锁死，请使用以下带有 `--no-daemon` 的命令构建 Release APK：
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease -x lintVitalAnalyzeRelease --no-daemon
```
*(默认输出路径：`D:\LsLife\releases\` 或对应的 build/outputs 目录)*

---

## 🎯 二次开发交接与下一阶段任务 (Next Stage Handover)

在下一轮开启的新对话中，新接手的架构师/Agent 请基于当前极度稳定且 UI 质感统一的 **V7.24** 基准代码，优先推进以下硬核攻坚战：

1. **层级地址选择器 UI 落地 (Cascading Address Picker)**
   - **目标**：在 Android 客户端的发帖页面、同城动态发布页、商家入驻页，实现一套基于“省-市-县-镇”四级的滑动选择器（配合 Material 3 BottomSheet）。
   - **痛点**：因为废弃了 LBS 地图 SDK 定位，必须提供流畅的文本地址录入体验。
2. **AI 图片处理管线工具化 (Image Pipeline Tooling)**
   - **目标**：当前还有如房屋出租、便民维修等部分占位图片需要重塑。请封装固化之前的 Python (`rembg` + `Pillow`) 512x512 包围盒自动去背景裁剪脚本，使其成为可一键批量调用的标准 CLI 工具。
