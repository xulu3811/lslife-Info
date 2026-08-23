# 同城•连山 (LianShan Local Services) - V6.17 深度交接与架构总结文档

## 📌 项目定位与现状 (Project State & Objective Reality)
本项目是一款针对县域级市场（目标覆盖人口 3~10 万，高并发支撑 3 万）量身打造的**本地同城分类信息与生活服务平台**。

历经多个版本的爆发式演进，项目现已全面跨越至 **V6.17 (全链路风控修复与 AI 视觉重塑版)**。
在具备区块链防篡改存证与底层动态表单数据流的基底上，最新版本实现了多项核爆级跨越：
1. **AI 视觉引擎与商业级图标重塑**：彻底摒弃了传统的扁平化 SVG。利用 `rembg` 与 Python 自动化脚本，对“首页金刚区”及“家政/护理”等二级类目的 15+ 张实物网图进行了自动去背、包围盒极限裁剪、15% 呼吸感留白与 512x512 黄金比例重采样。客户端 UI 质感直逼一线大厂。
2. **发布模块与鉴权上下文 (Context) 修复**：深入修复了 `quota.ts` 中间件中 Prisma 查表的 `select` 限制导致的 `realNameStatus` 数据丢失断层，彻底解决了“已实名却被误拦截不可发布”的 Bug。
3. **超管特权穿透 (Bypass) 修复**：在 `publish.ts` 中补齐了 `SUPERADMIN` 逻辑，超管账号 (如 13828577665) 现可无视限制发布任何测试内容。
4. **服务器底层架构净化**：利用 `deploy_clean.mjs` 强制杀死了导致端口阻塞的 Node 僵尸进程，通过 `npx prisma db push` 在阿里云 PostgreSQL 服务器底层补齐了 `PromotionTask` 等缺失表结构。
5. **包体极限瘦身**：配合 Gradle `shrinkResources` 无用资源裁剪机制，彻底剔除旧版废弃资源，Release APK 极限瘦身至 5MB 以内。

当前系统服务端平稳运行于阿里云生产环境。客户端核心编译产物统一自动归档于 `D:\LsLife\releases\`。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. 统一合规审批流与发布拦截 (Unified Approval & Risk Control)
*   **强鉴权拦截**：在 `publish.ts` 结合 `requireQuota` 中间件，严格要求用户 `realNameStatus === 'verified'` 或 `isMerchant === true` 才能发帖，非验证用户只能浏览。
*   **多层级审批**：管理后台 `/dashboard` 提供帖子审查、个人实名(KYC)、商家入驻(Merchant Cert) 的精细化审查。
*   **状态机流转**：`AI_REVIEWING` (审核中) -> `MANUAL_REVIEWING` (待人工) -> `PUBLISHED` (已发布) / `REJECTED` (已拒绝)。

### 2. 千人千面：基于 JSONB 的动态属性表单 (Dynamic Attributes Schema)
*   **非结构化存储**：PostgreSQL 底层使用 `attributes: Json?` 字段，实现 Schema-Free 扩展。
*   **商业级 UI 解析呈现**：客户端通过 `CategorySchemaRegistry` 动态读取映射规则，在详情页将复杂的 `Jsonb` 结构渲染成高对比度、带有微边框多选标签的高级数据表格。

### 3. UI/UX 视觉引擎：3D Soft UI 与实物级排版 (Joybuy UI Aesthetics)
*   **统一组件库沉淀**：严格统一使用 `12dp` 圆角、`0.5dp` 细高亮阴影、`13.5sp` 中等字重。
*   **实物级 Icon 渲染**：利用 Coil `AsyncImage` + `CircleShape` + `Color(0xFFEEEEEE)` 微边框，完美呈现 AI 扣除背景后的实物 PNG 图标。

### 4. 深度即时通讯与区块链级交易存证 (Deep IM & Blockchain Storage)
*   **极速图片传输**：本地无损压缩 -> Base64 文本流 -> WebSocket 直连。
*   **区块链防篡改**：后端实施 **AES-256-CBC 对称加密** 落盘，利用前一条消息的哈希计算 **SHA-256 级联哈希**，构成不可篡改证据链。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend - `D:\GitHub-lslife-V6.0\android\`)
*   **语言 / 核心 SDK**: Kotlin / Min SDK 24 / Target SDK 34 / JDK 17 (Android Studio jbr)
*   **UI 框架**: Jetpack Compose / Material3 / 全局 `NavHost` 路由
*   **架构 / 状态管理**: MVVM 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow
*   **网络与解析**: Retrofit2 (RESTful) / OkHttp3 / WebSockets / **Kotlinx Serialization**

### 服务端与数据存储 (Backend - `D:\GitHub-lslife-V6.0\backend\`)
*   **环境 / 框架**: Node.js / Express / TypeScript / PM2 热载托管
*   **数据库 / ORM**: PostgreSQL / Prisma ORM
*   **部署架构**: 阿里云 Ubuntu 24.04 (IP: `115.191.6.95`)，Docker PostgreSQL (宿主机 5433 -> 容器 5432)。
*   **自动化热更新**: 使用本地 `deploy_clean.mjs` 脚本一键清空远端缓存、编译代码、强制同步 Prisma DB 结构并重启 PM2 `lslife-api` 守护进程。

---

## 🗄 服务器与数据库基础设施 (Infrastructure & DB Specs)

1. **服务器信息**:
   * **IP**: `115.191.6.95` (Ubuntu 24.04)
   * **账号**: `root` (密码：`Maxence2468;`)
2. **数据库信息 (PostgreSQL - Docker)**:
   * **数据库名**: `lslife` | **用户名**: `lslife` | **密码**: `af4a98b163543c58c46bf827bdd546a8`
   * **端口**: 宿主机 `5433` -> 容器 `5432`
3. **管理后台**:
   * **Web 管理端**: `https://mentalhlp.site/admin-web/`
   * **App 端入口**：“我的” -> “平台运营与管理” (超级管理员账号: `13828577665`)

---

## 🚀 自动化编译与发版指引 (Build Rules)

*   **客户端编译指令 (必须在 PowerShell 中执行)**：
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease -x lintVitalAnalyzeRelease
  ```
  *(注：使用 `-x lintVitalAnalyzeRelease` 避开 Windows 文件锁)*
*   **Release APK 默认输出路径**：`D:\LsLife\releases\`
*   **后端自动化部署指令**：
  在 `backend` 目录下执行 `cmd /c npm run build` 后，通过 `node deploy_clean.mjs` 热推向服务器。

---

## 🎯 二次开发交接与下一阶段任务 (Next Stage Handover)

接手此项目进行**新一轮二次开发**的架构师/Agent，请基于当前已完全打通闭环的 **V6.17** 版，优先开启以下新工作：

1. **IM 地图与 LBS 定位卡片 (Map & LBS Location)**
   - 客户端接入高德/腾讯地图 API，实现消息 `type="location"` 的地图卡片互传。
   - 基于经纬度的同城商品或商家的“附近的人”距离展示（可结合 PostgreSQL 的 PostGIS 或 GeoJSON 计算）。
2. **瀑布流商品卡片的交互强化**
   - 进一步完善图文卡片的占位图骨架屏 (Skeleton Loading)、长图智能裁剪展示，以及底部关联 `linkedCommerceId` 实体商品的直达入口。
3. **AI 图片处理自动化扩充**
   - 针对后续仍需替换的子分类图标（如房屋出租、便民维修等），可继续复用 Python (`rembg` + `Pillow`) 的 512x512 智能包围盒裁剪管线，确保 App 整体调性的 100% 统一。
   - **???????? (Icon Processing Standard)**:
     - **???**:Python + embg + Pillow
     - **????**:
       1. **??? (Background Removal)**:?? embg.remove ????????(?????????)?
       2. **???? (Tight Bounding Box)**:?? getbbox() ??????????????????
       3. **????? (Standard Canvas)**:?? 512x512 ????? RGBA ???
       4. **????? (Safety Padding)**:???? 24px ??????,?????????? 464x464?
       5. **???? (Proportional Scaling)**:?? Image.Resampling.LANCZOS ??????????????? 464px?
       6. **???? (Absolute Centering)**:????????????????
       7. **?? (Output)**:????? .png,????? Android res ???
