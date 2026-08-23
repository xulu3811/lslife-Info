# 同城•连山 (LianShan Local Services) - V4.00 深度交接与架构总结文档

## 📌 项目定位与当前演进状态 (Project State & Evolution)
本项目是一款专注于县域级市场（下沉市场，覆盖人口 3~10 万，支撑 3 万高并发）的高端**本地同城分类信息与生活服务平台**。

历经多个大版本的爆发式迭代，项目现已全面进化至 **V4.00 (全栈深度重构与 Joybuy 级极简美学版)**。
在具备区块链防篡改存证与高保真实物抠图的基底上，V4.00 实现了三大核爆级跨越：
1. **基于 DeepSeek AI 引擎的先审后发双重风控系统**；
2. **底层基于 Postgres JSONB 的动态发布属性架构**；
3. **客户端界面彻底向国际化 Joybuy/Uber 式 3D Soft UI (软阴影极简美学) 的颠覆性升级**。

当前系统已完成全链路修复与测试，服务端平稳运行于阿里云，客户端最新产物为 `LsLife-v4.00-release.apk`。

---

## 🏗 核心模块、底层逻辑与核心算法 (Core Architecture & Logic)

### 1. 先审后发：DeepSeek AI + 人工双重审查引擎 (AI Risk Control)
*   **状态机流转 (State Machine)**：重构了 `PostStatus` 逻辑，发布流程升级为：`AI_REVIEWING` (审核中) -> `MANUAL_REVIEWING` (待人工) -> `PUBLISHED` (已发布) / `REJECTED` (已拒绝)。
*   **DeepSeek 语义审查**：后台集成 DeepSeek (OpenAI SDK 兼容) 大语言模型。当 DFA 本地敏感词轻核通过后，通过异步 Node.js 队列调用大模型进行涉政、涉黄、暴恐深度语义剖析。
*   **人工终审后台**：管理端提供待审队列，人工确认放行后才对外透出。

### 2. 千人千面：基于 JSONB 的动态属性表单 (Dynamic Attributes Schema)
*   **非结构化存储**：底层数据库在 `Post` 模型中启用了 PostgreSQL 特有的 `attributes: Json?` 字段，实现 Schema-Free 的扩展。
*   **动态渲染协议**：不同二级分类（如二手房产、兼职求职、二手手机）在客户端渲染发布模块时，会动态拉取属于自己的属性模板表单（如：成色、户型、期望薪资），并统一打包序列化至数据库的 Json 字段中。

### 3. UI/UX 视觉引擎：3D Soft UI 与极简排版重塑 (Joybuy UI Aesthetics)
*   **Modern Category Header**：彻底推翻旧版 3 行堆叠布局，采用“折叠空间”思想，将筛选与搜索收纳进极浅灰色 (`#F4F5F7`) 的药丸形组件中，释放了大量屏幕高度。
*   **InfoPublishCard / ServiceListFeedCard**：全面迈入 3D 悬浮无边框时代。卡片标配 `16dp` 圆角，辅以 `8dp` 超细腻弥散环境光阴影 (`0x0F000000`)。
*   **全局去广告化**：彻底清除了分类详情与流列表中的“【广告位】连山新楼盘/同城车展”横幅，将空间 100% 让渡给同城本地生活内容。
*   **Bottom Action Bar 黄金比例**：重构了商品详情页的底部操作栏。即使浏览自己发布的商品，依然保持“收藏”、“私聊”、“电话” 三分天下的黄金视觉比例，并通过 Toast 优雅实现私聊防呆拦截。

### 4. 深度即时通讯与区块链级交易存证 (Deep IM & Blockchain Storage) - (继承自 V2/V3)
*   **极速图片传输**：本地无损压缩 -> Base64 文本流 -> WebSocket 直连，毫秒级上屏。
*   **区块链级防篡改存证**：后端实施 **AES-256-CBC 对称加密** 落盘，利用前一条消息的哈希计算 **SHA-256 级联哈希**，构成不可篡改证据链。
*   **独占式保活**：客户端采用独占 WebSocket 保活，配合 `IMPORTANCE_HIGH` 系统级通知。

---

## 💻 技术栈底座与架构 (Technology Stack)

### Android 客户端 (Frontend - `D:\LsLife\android\`)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34 / JDK 17 (构建工具: Android Studio jbr)
*   **UI 框架与路由**: Jetpack Compose / Material3 / 全局 `NavHost` 路由 (所有路径参数强制 URLEncoder)
*   **架构 / 状态管理**: 严格的 MVVM 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow
*   **网络通信**: Retrofit2 (RESTful) / OkHttp3 / WebSockets (全双工保活通信)
*   **图像渲染引擎**: Coil

### 服务端与数据存储 (Backend - `D:\LsLife\backend\`)
*   **环境 / 框架**: Node.js / Express 兼容层 / TypeScript / PM2 热载与崩溃重启托管
*   **数据库 / ORM**: PostgreSQL 关系型数据库 (极度依赖其 Jsonb 特性) / Prisma ORM
*   **部署环境**: 阿里云 Ubuntu 24.04 (IP: `115.191.6.95`)，Docker PostgreSQL 容器 (宿主机端口 5433 映射至容器 5432)。

---

## 🗄 服务器与数据库基础设施 (Infrastructure & DB Specs)

1. **服务器信息**:
   * **IP**: `115.191.6.95` (Ubuntu 24.04)
   * **账号**: 运维账号 `lslife` (已配置 SSH 公钥免密) / 拥有全权 ROOT 权限
2. **数据库信息 (PostgreSQL - Docker)**:
   * **数据库名**: `lslife` | **用户名**: `lslife` | **密码**: `af4a98b163543c58c46bf827bdd546a8`
   * **映射端口**: 宿主机 `5433` -> 容器 `5432`
3. **管理后台**:
   * **地址**: `https://mentalhlp.site/admin-web/`
   * **管理员账号**: `root` | **密码**: `NtktiC726Kbmt3oMM8B5EgVQ`

---

## 🚀 自动化编译、发版与规范 (Build & Routing Rules)

*   **发版归档**：Release 编译产物统一归档至 `D:\LsLife\releases\` 目录。项目根目录 `version.properties` 会在打包时自动递增 VersionCode（最新包为 V4.00 架构版本）。
*   **标准编译指令 (必须在 PowerShell 中执行)**：
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease -x lintVitalAnalyzeRelease
  ```
  *(注：使用 `-x lintVitalAnalyzeRelease` 参数是为避开 Windows 文件锁与 IDE lint-cache 占用导致编译报错的核心黑科技)*
*   **路由规范 (Navigation)**：任何向 Jetpack Compose 传递的 String 参数，**必须**使用 `java.net.URLEncoder.encode(..., "UTF-8")` 封装，禁止裸奔导致栈崩溃！

---

## 🎯 二次开发交接与下一阶段指引 (Next Stage Handover)

接手此项目进行**二次开发**的架构师/Agent，请基于当前极度稳定、高度唯美的 V4.00 盘，优先规划并开启以下新工作：

1. **动态属性表单 (Dynamic Attributes) 的深度透出**
   - 当前底层 JSONB 与发布端已支持。下一阶段需要在 `PostDetailScreen` (商品详情页) 中，将这些存入 JSONB 的动态属性（如“成色：99新”、“月薪：5k-8k”）优雅地渲染展示出来。
2. **同城动态 Feeds 流双列瀑布流渲染 (Moment Feed UI)**
   - 继续优化 `CityStrollFeedScreen`，全面对标小红书的“双列瀑布流图文卡片”，并透出所关联的 `linkedCommerceId` 闭环。
3. **全栈包体瘦身 (Icon Asset Thinning)**
   - V3 引入的大量实物抠图占据了不小体积，需写 Python 脚本对 drawable 中的资源做进一步 WEBP 压缩。
4. **IM 地图与 LBS 定位卡片 (LBS Engine)**
   - 接入高德/腾讯地图 API，实现 `type="location"` 地图卡片发送，及基于经纬度的同城商品距离检索。
