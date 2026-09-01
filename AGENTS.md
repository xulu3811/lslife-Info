# 同城清远 (Qingyuan Smart Local Life Service Platform) - V8.36 深度交接与架构总结文档

## 📌 项目定位与当前状态 (Project State & Objective Reality)
本项目是一款针对县域级市场（目标覆盖人口 3~10 万，高并发支撑 3 万）量身打造的**纯净本地同城分类信息与生活服务平台**。

历经多个版本的硬核迭代，项目已全面跨越至 **V8.36 (原生秒开与全系 Material 3 纯净视觉版)**。
在重构了 Web 管理后台和完善 OTA 自动升级闭环后，近期我们在客户端层面的极致性能与 UI 统一上取得了决定性成果：
1. **商业推广模块彻底 M3 化**：对“我的钱包”、“超级会员”、“推广中心”等核心页面进行了彻底重构。移除了早期的“淘宝式大红尖角标签”与硬编码颜色，全系采用 MaterialTheme.colorScheme 的 primaryContainer、	ertiaryContainer 等动态语义色。
2. **全局货币符号精简**：通过底层 strings.xml 将全站的“清远币”统一重构为极简的 **PC**，并重绘了单行平滑式的资产概览卡片，UI 空间利用率大幅提升。
3. **首页秒开架构 (Offline-First Cache)**：引入本地强缓存引擎，冷启动 **0毫秒瞬间渲染** 首页瀑布流历史数据，彻底终结了 1-2 秒的骨架屏等待期。
4. **原生轻量化 GPS 引擎**：彻底抛弃高德/腾讯等沉重的 LBS 地图 SDK，使用纯粹的原生 LocationManager 结合正则提取算法，直接输出“镇/街道”级纯文本。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. 首页秒开与本地强缓存引擎 (Offline-First Cache)
*   **极致体验**：利用 SharedPreferences + Kotlinx Serialization 将首页推荐列表静默落盘。ViewModel 在 init 阶段瞬间反序列化并渲染数据，实现“断网可见、秒级开屏”，后台再静默覆盖最新数据。

### 2. 轻量化原生位置解析算法 (Native Geolocation)
*   **脱离 Map SDK**：封装 LocationHelper.kt。
*   **正则行政区划提取**：通过正则 (?<=县|区|市)[^县区市]+?(镇|街道|乡) 直接将地理位置转换为下沉市场用户最关心的乡镇级纯文本，极大幅度减小了 APK 包体积和内存占用。

### 3. 千人千面：基于 JSONB 的动态属性表单 (Dynamic Attributes Schema)
*   **非结构化存储**：底层 PostgreSQL 采用 ttributes: Json? 字段，实现免 Schema 变更的无限分类扩展。客户端利用 CategorySchemaRegistry 在 Compose UI 中动态渲染多选标签与表单。

### 4. 深度即时通讯与区块链级交易存证 (Deep IM & Blockchain Storage)
*   **安全通信协议**：基于 WebSocket 直连，后端执行 **AES-256-CBC** 对称加密。利用上一条消息的哈希值计算 **SHA-256 级联哈希**，构建不可篡改的消息证据链。

### 5. 统一合规审批流与发布拦截 (Unified Approval & Risk Control)
*   **强鉴权拦截**：Node.js 后端强行挂载风控中间件，系统通过 AI_REVIEWING -> MANUAL_REVIEWING -> PUBLISHED 的严格状态机控制非法内容外流。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34 / JDK 17
*   **UI 框架**: Jetpack Compose / Material3 / 响应式动态色彩主题
*   **架构**: MVVM / MVI 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow
*   **网络与持久化**: Retrofit2 / OkHttp3 / WebSockets / Room DB (本地聊天缓存)

### 服务端 Web 与 API (Backend)
*   **环境 / 框架**: Node.js / Express / TypeScript / PM2 热载托管
*   **Web 框架 (admin-web)**: React / Vite / 纯 CSS3 自定义 M3 样式
*   **数据库 / ORM**: PostgreSQL (5432) / Prisma ORM
*   **服务器端点**: API BaseURL: https://mentalhlp.site/api/

---

## 🛑 平台红线与开发原则 (Core Platform Rules)

> **以下为同城清远体系的绝对红线，新接手的 Agent 或开发者不得违背：**

1. **纯信息发布平台，严禁电商闭环**：
   - 定位于纯信息发布与撮合平台，绝对**不提供**在线电商商品交易闭环。
   - 严禁引入“购物车 (CartItem)”、“订单 (Order)”、“物流发货 (Delivery)”表结构。
   - 支付仅限平台虚拟货币 (PC) 的充值及购买平台增值服务（发帖配额、置顶）。
2. **轻量化位置体系，坚决废弃 LBS 地图 SDK**：
   - 严禁引入任何第三方重型地图 SDK。只能使用 Android 原生定位框架或纯文本解析。
3. **严格遵守 Material 3 设计语言**：
   - Android 端必须使用 MaterialTheme.colorScheme 中的动态语义色，严禁在业务模块中重新定义“淘宝红”、“硬编码白”等破坏全局深浅色切换的颜色。所有组件需保持留白与高级感。

---

## 🚀 自动化编译与发版指引 (Build Rules)

**客户端安全编译指令 (必须在 PowerShell 中执行)**：
为了防止内存溢出、文件锁死、Hilt 增量缓存失效以及中文乱码（Mojibake），请**强制**使用以下 Clean 全量构建指令（并避免使用 PowerShell 原生字符串替换包含中文的文件，请使用 Python 或专用工具）：
`powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; cd d:\GitHub-lslife-V6.0\android; .\gradlew.bat clean assembleRelease -x lintVitalAnalyzeRelease --no-daemon --no-configuration-cache
`

---

## 🎯 二次开发交接与下一阶段任务 (Next Stage Handover)

基于当前极具高级感和流畅度的 **V8.36** 基线代码，下一轮开启新对话的架构师/Agent 请立刻推进以下攻坚战：

1. **纯文本层级地址选择器 UI 落地 (Cascading Address Picker)**
   - **状态**：目前“一键获取原生定位”已在首页和发布页跑通，且自动提取乡镇。
   - **目标**：用户在发帖页如果定位失败，需要手动点击“请选择 >”，此时必须弹出一个基于“省-市-县-镇”四级的滑动/列表选择器（结合 M3 BottomSheet）。纯本地化文本驱动，无需地图渲染。
2. **Prisma 数据库迁移与新字段同步**
   - **目标**：确保线上 PostgreSQL 环境 (
px prisma db push) 结构与 Schema 同步，为下一阶段的新业务（如商家入驻扩展字段）做好准备。
