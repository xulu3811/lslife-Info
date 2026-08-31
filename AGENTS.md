# 同城清远 (Qingyuan Smart Local Life Service Platform) - V9.0 深度交接与架构总结文档

## 📌 项目定位与当前状态 (Project State & Objective Reality)
本项目是一款针对县域级市场（目标覆盖人口 3~10 万，高并发支撑 3 万）量身打造的**纯净本地同城分类信息与生活服务平台**。

历经多个版本的硬核迭代，目前项目已全面跨越至 **V9.0 (Material 3 重构与架构补全版)**。
我们在 V8.0 品牌重塑的基础上，彻底完成了 Web 管理后台的现代化改造，并成功构建了 Android 客户端的 OTA 自动升级闭环：
1. **Web 管理后台彻底重构为 Google Material 3 规范**：废弃了早期冗余且割裂的浮雕、玻璃拟态样式（Tailwind + CSS 混编），统一升级为原汁原味的 Google Workspace 风格。所有页面（包括 dashboard、实名认证审核、用户管理、系统安全控制、App版本管理、分类管理等）已实现高度一致的设计语言。
2. **Dashboard 科技感数据大屏**：打通了 `/admin/server-status` 与前端 Dashboard，实现了高频轮询展示服务器 CPU、内存、磁盘以及 PM2 后端进程存活状态的实时监控图表。
3. **OTA 热更新极速触达与闭环**：完善了 Web 端的 APK 拖拽/上传、MD5 校验与 URL 回填功能。客户端通过 WebSocket 实时监听 `APP_UPDATE_AVAILABLE` 指令，实现了后台一键强制或静默发版。
4. **编译链路极致优化**：全面应用 `--no-daemon` 策略对抗 R8 混淆器导致的内存溢出，并在全局重构后平稳渡过了 Dagger/Hilt 依赖注入框架的缓存失效期。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. 统一合规审批流与发布拦截 (Unified Approval & Risk Control)
*   **强鉴权拦截**：在 Node.js 后端的 `publish.ts` 中结合 `requireQuota` 中间件，严格要求用户必须是 `realNameStatus === 'verified'` 或 `isMerchant === true` 方可发帖，并特别保留了超级管理员的无视限制穿透 (Bypass) 权限。
*   **状态机流转**：系统通过 `AI_REVIEWING` -> `MANUAL_REVIEWING` -> `PUBLISHED` 的状态机进行内容风控管控。

### 2. 千人千面：基于 JSONB 的动态属性表单 (Dynamic Attributes Schema)
*   **非结构化存储**：底层 PostgreSQL 采用 `attributes: Json?` 字段，实现免 Schema 变更的无限扩展。
*   **动态渲染**：客户端利用 `CategorySchemaRegistry` 解析动态规则，在 Compose UI 中将其渲染成高级数据表格，支持多选高亮标签等复杂 UI。

### 3. AI 视觉引擎与 3D Soft UI (Aesthetics & AI Processing)
*   **智能包围盒裁剪管线 (AI Icon Processing)**：摒弃传统扁平 SVG，利用 Python (`rembg` + `Pillow` 结合 Floodfill 算法) 对实物网图进行去背、极限裁剪，并统一输出为 512x512 包含留白呼吸感（Padding: 58px）的 RGBA 透明底 PNG。
*   **Google Material 3 (Web 端)**：全面应用 `--g-blue`、`--g-red` 等标准色，以及 `md-card`、`md-input`、`md-btn` 等模块化原子 CSS 类，确保在 Chrome/Edge 浏览器下提供极度顺滑、克制的高级企业级后台体验。

### 4. 深度即时通讯与区块链级交易存证 (Deep IM & Blockchain Storage)
*   **通信协议**：基于 WebSocket 的直连即时通讯，配合本地无损图片压缩与 Base64 文本流极速传输。
*   **语音流媒体化**：通过后端 `/chat_audio` 静态路由直接投递 mp4/m4a 流媒体，客户端通过 MediaPlayer 处理 https 安全直连。
*   **区块链级存证**：后端在落盘时执行 **AES-256-CBC** 对称加密，并利用上一条消息的哈希值计算 **SHA-256 级联哈希**，构建不可篡改的消息证据链。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34 / JDK 17
*   **UI 框架**: Jetpack Compose / Material3 / 全局 NavHost 路由
*   **架构**: MVVM / 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow / Coil
*   **网络与持久化**: Retrofit2 / OkHttp3 / WebSockets / Kotlinx Serialization / Room DB (`LocalConversationEntity`, `LocalMessageEntity`)

### 服务端 Web 与 API (Backend)
*   **环境 / 框架**: Node.js / Express / TypeScript / PM2 热载托管
*   **Web 框架 (admin-web)**: React / Vite / 纯 CSS3 自定义样式 (已移除冗余第三方框架)
*   **数据库 / ORM**: PostgreSQL (5432, 外部隔离) / Prisma ORM
*   **服务器交互**: Web 端与后端的部署分离，API BaseURL: `https://mentalhlp.site/api/` 或开发态下的 `/api` 相对路径代理。

---

## 🛑 平台红线与开发原则 (Core Platform Rules)

> **以下为同城清远体系的绝对红线，任何 Agent 或开发者不得违背：**

1. **纯信息发布平台，严禁电商闭环**：
   - 本项目定位于纯信息发布平台，绝对**不提供**在线电商交易闭环。
   - 严禁引入或使用任何“购物车 (CartItem)”、“订单 (Order)”、“在线支付流水 (Payment)”、“物流发货 (Delivery)”逻辑与表结构。
   - 任何涉及资金流动的仅限平台自身服务（例如：购买发帖配额、置顶帖子、商家入驻认证），不包含物理商品的 C2C/B2C 交易。
2. **轻量化位置体系，坚决废弃 LBS 地图 SDK**：
   - 客户端严禁引入高德、腾讯等重型 3D/2D 地图 SDK，因为这将导致包体积暴增且增加下沉市场的维护成本。
   - 位置信息不使用基于 `latitude` / `longitude` 的 Haversine 球面距离计算。
   - 彻底转为轻量级文本层级方案。
3. **Web 端严禁引入新的重量级 CSS 框架**：
   - 所有的 UI 请复用 `admin-web/src/index.css` 中定义好的 `md-*` 规范库，严禁随便写入难以维护的 inline-style 或者重新引回 Tailwind 类。

---

## 🚀 自动化编译与发版指引 (Build Rules)

**客户端安全编译指令 (必须在 PowerShell 中执行)**：
为了防止内存溢出、文件锁死以及 Hilt 增量缓存失效，请**强烈建议**在修改后执行 Clean 全量构建：
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; cd android; .\gradlew.bat clean assembleRelease -x lintVitalAnalyzeRelease --no-daemon
```
*(默认输出路径：`D:\GitHub-lslife-V6.0\android\app\build\outputs\apk\release\app-release.apk`)*

**服务端 Web 编译与后端安全部署指令 (需在 admin-web / backend 目录执行)**：
```powershell
# 在 admin-web 目录下执行：
npm.cmd run build
# 然后切换到 backend 目录下执行：
node deploy_web.cjs
node deploy_clean.mjs  # 若修改了后端逻辑
```

---

## 🎯 二次开发交接与下一阶段任务 (Next Stage Handover)

在下一轮开启的新对话中，新接手的架构师/Agent 请基于当前完美的 **V9.0** 基准代码，立刻推进以下硬核攻坚战：

1. **层级地址选择器 UI 落地 (Cascading Address Picker)**
   - **目标**：在 Android 客户端的发帖页面、同城动态发布页、商家入驻页，实现一套基于“省-市-县-镇”四级的滑动选择器（配合 Material 3 BottomSheet）。
   - **痛点**：因为废弃了 LBS 地图 SDK 定位，必须提供流畅且纯本地化的文本地址层级录入体验。此任务为最高优先级！
2. **AI 图片处理管线工具化 (Image Pipeline Tooling)**
   - **目标**：当前还有部分分类的占位图片需要重塑。请封装固化之前的 Python (`rembg` + `Pillow`) 512x512 包围盒自动去背景裁剪脚本，使其成为可一键批量调用的标准 CLI 工具。

