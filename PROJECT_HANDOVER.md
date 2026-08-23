# 连山同城 (LsLife) V6.0 核心交接与架构白皮书

> **目的**：本文档是对本项目当前开发进度、技术底座、核心算法及服务器配置的深度总结。**任何接手本项目进行二次开发的新 AI 助手 (Agent) 或工程师，请务必在开始编写代码前仔细阅读此文档，以确保技术栈不发生断层，且遵循既有的代码规范与底层架构。**

---

## 一、 项目状态与客观现实 (Project State)
本项目是一款针对县域级市场（目标覆盖人口 3~10 万，并发支撑 3 万）量身打造的**本地同城生活服务交易与通讯平台**。
历经多个版本的爆发式演进，项目现已全面升级至 **V5.0 (Joybuy 级商业视觉体验与全品类覆盖版)**。
现阶段系统已极其稳定，且已成功部署至阿里云生产环境。即将开启 V6.0 阶段的深度二次开发。

---

## 二、 架构与技术栈底座 (Technology Stack)

### 1. Android 客户端 (Frontend)
*   **开发语言 / 环境**: Kotlin / JDK 17 / Min SDK 24 / Target SDK 34
*   **UI 框架**: 纯 **Jetpack Compose**，搭配 Material3 设计系统。
*   **核心架构**: 严格遵循 **MVVM 单向数据流 (UDF)**，通过 **Dagger Hilt** (`@HiltViewModel`, `@AndroidEntryPoint`) 注入依赖，基于 `Coroutines` 和 `StateFlow` 进行状态分发。
*   **网络与通信**: `Retrofit2` (RESTful) 搭配 `OkHttp3` 处理短链接；原生的 **WebSocket** 引擎实现全双工保活与即时通讯。
*   **媒体引擎**: 采用 **Coil** 进行图像内存级渲染，并对相机 (`CameraX`) 与媒体音频 (`AudioManager`) 进行了底层重写。
*   **路由**: AndroidX Navigation。**【强制规范】** 任何带有特殊字符的参数传递必须经过 `java.net.URLEncoder.encode` 编码处理。

### 2. 服务端 (Backend)
*   **核心引擎**: Node.js / Express / TypeScript。
*   **进程守护**: PM2 托管热启动 (`ecosystem.config.cjs`)。
*   **加密与安防**: Node.js 原生 Crypto 模块 (AES-256 + SHA-256) 用于消息防篡改及用户数据脱敏。

### 3. 数据库引擎与 ORM (Database)
*   **数据库**: **PostgreSQL**。
*   **ORM 模型**: **Prisma ORM** (`schema.prisma`)。

---

## 三、 核心模块、业务逻辑与创新算法 (Core Features & Algorithms)

### 1. 交易流转与购物车防脏数据引擎
*   **互斥购物车算法**：购物车 (`CartViewModel`) 采用独立 ID 追踪。底层防跨店算法在勾选新商铺商品时，会自动触发互斥逻辑，清空其他商铺的勾选项，确保生成的 `Order` 合法。
*   **收货地址生命周期联动**：结算页通过 `LifecycleEventObserver` 监听 `ON_RESUME`，实现用户跳转填写地址后的无缝自动回填。

### 2. 深度即时通讯与区块链级存证 (Deep IM)
*   **极速图像流转机制**：针对 HTTP 图片上传缓慢的痛点，采用 **“本地高强度压缩 -> Base64 文本流 -> WebSocket 直连服务器分片投递”** 算法。客户端拦截流后直接输入给 Coil 引擎，达到毫秒级上屏体验。
*   **高压原声 AAC 通信**：重写了原生的语音交互框架，手搓 `pointerInput` 手势拦截识别，复刻了国民级 App 的“按住说话、上滑取消”交互，文件落盘为 m4a 格式。
*   **区块链级防篡改哈希链**：聊天存仓 (hub.ts) 全程实施 **AES-256-CBC** 加密落盘。每条信息基于“前一条 Hash + 当前明文”计算出唯一的 **SHA-256** 哈希 (`evidenceHash`)，形成数字证据链。
*   **系统级进程保活**：`LsLifeImService` 绑定为前台服务 (Foreground Service)，保障进程存活率以及系统级私聊高优先级通知 (`IMPORTANCE_HIGH`)。

### 3. AI 驱动的动态发布引擎 (Dynamic Publish)
*   **多态分类驱动**：不同于传统的死板表单，发布页利用 Prisma 的 `Category` 内置 `attributeSchema`，实现对房产、二手、兼职、拼车等品类的 **动态条件渲染**。
*   **DeepSeek AI NLP 中枢**：接入 AI (`ai.ts`) 大模型能力。用户输入非结构化的大段文本，AI 自动提取品牌、成色、价格、期望等要素，一键结构化回填表单。

### 4. V5.0 高规格视觉引擎 (Joybuy UI)
*   全面贯彻“极简克制”的设计语言，采用 `#FFFFFF` 容器底色配合浅灰隔离。
*   移除了繁杂背景，改用扁平化微雕圆角，以及醒目的红色加购按钮，突显商品与 3D 高规格分类图标本身。

---

## 四、 关键环境资产与密钥 (Infrastructure Assets)

接手的 Agent 需要在必要时调用以下资产（**严禁在未经脱敏的代码或截图中外泄**）：

1.  **后端主服务器**: `115.191.6.95` (Ubuntu 24.04)。SSH 账号：`lslife` (公钥免密)。
2.  **管理后台入口**: `https://mentalhlp.site/admin-web/` （超管：`root` / `NtktiC726Kbmt3oMM8B5EgVQ`）
3.  **生产数据库 (PG)**:
    *   Docker 宿主机端口 `5433` 映射至容器 `5432`。
    *   Credentials: User=`lslife` / DB=`lslife` / Pass=`af4a98b163543c58c46bf827bdd546a8`
4.  **DeepSeek AI API**: `sk-30f79d21acbd487da71ec3cb5ce63d54`

---

## 五、 V6.0 阶段二次开发强制指引 (Mandatory Next Steps)

进入新的对话框进行后续开发时，务必首先围绕以下方向展开，且**不可破坏原有的 MVVM + Hilt 架构**：

1.  **数据容灾与自动化备份 (CRITICAL)**
    *   当前项目已处于全量跑通状态，首要任务是必须在服务器端建立针对 PostgreSQL 数据库以及 `public/assets/` 静态图片的增量/全量 Cron 定时备份脚本，防止任何数据雪崩。
2.  **基于 Prisma 的高级联合检索 (Advanced Search)**
    *   由于发布数据已完全结构化存入 `attributes`，下一阶段需开发支持多维过滤的高级搜索页（例如：“价格区间” + “新旧成色” + “所属大类”），请充分发挥 Prisma 的复杂复合查询与 JSON 原生过滤能力。
3.  **O2O 定位与地图卡片闭环 (Map Ecosystem)**
    *   IM 通讯协议中已前置预留了位置字段。下一阶段需接入 高德地图 / 腾讯地图 Android SDK，在聊天气泡中实现 `type="location"` 的地图卡片双向互发，并引入商家距离筛选计算机制。
4.  **权限规范**
    *   一切针对相机、麦克风、地理位置的新增权限，必须通过 Compose 官方的 `ActivityResultContracts` 进行安全且合规的动态获取请求。
