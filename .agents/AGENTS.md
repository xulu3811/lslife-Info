# 🏢 清远智慧同城生活服务平台项目 (V1.0) 深度交接与全栈架构白皮书

> **文档用途**：本白皮书旨在为下一次会话（Secondary Development）的新 Agent 或架构师提供最详尽、最底层的系统全景图。请下一任接手者仔细阅读本说明，以保证项目风格与底层逻辑的绝对延续。

---

## 📌 一、 项目定位与当前状态 (Project State)

本项目定位于**县域下沉市场的纯净版同城分类信息与生活服务撮合平台**。
在最近的研发迭代中，项目完成了以下决定性跨越：
1. **全面跨入 V1.0 商业级基线**：全盘清除了历史版本的技术债与遗留硬编码字眼，正式更名为“清远同城 V1.0”，并已全套输出《软件著作权》所需的 60 页源代码鉴别材料与设计说明书文档。
2. **发布系统UI的美学重构**：彻底重构了发布界面的 `CategoryTreeBottomSheet`。全面拥抱 Material 3，引入了胶囊状紧凑型搜索栏（44.dp）、无边框 Surface 视觉流、以及基于 `primaryContainer` 的左侧动态导航高亮，UI 尺寸完美对齐首页骨架比例。

---

## 🏗 二、 核心底层逻辑与独家算法 (Core Logic & Algorithms)

### 1. 首页秒开：本地强缓存引擎 (Offline-First Cache)
- **底层逻辑**：针对下沉市场网络波动的痛点，抛弃传统的每次冷启动 Fetch 策略。
- **技术实现**：利用 `SharedPreferences` 结合 `Kotlinx Serialization` 将首页推荐流静默落盘。在 `ViewModel.init` 阶段，瞬间（0毫秒）反序列化并渲染上一帧历史数据，实现“断网可见、秒级开屏”，随后后台静默获取最新数据并平滑覆盖。

### 2. 脱钩地图SDK：原生轻量化位置解析 (Native Geolocation Regex)
- **底层逻辑**：严格遵从轻量化红线，坚决不接入高德/百度等沉重的第三方 LBS SDK，以防 App 极度膨胀。
- **技术实现**：封装 `LocationHelper.kt`，调用纯粹的原生 `LocationManager`。获取反向地理编码后，利用极简正则算法 `(?<=县|区|市)[^县区市]+?(镇|街道|乡)` 直接抽取出用户最关心的“乡镇级”纯文本标签。

### 3. 千人千面：无模式动态属性表单 (Schemaless JSONB)
- **底层逻辑**：房产、二手车、家政需要的表单字段截然不同，无法通过传统二维表固化。
- **技术实现**：PostgreSQL 底层统一采用 `attributes: JsonB?` 字段。Android 客户端基于 `CategorySchemaRegistry` 动态下发并渲染多选标签与输入表单，实现无限垂直品类的免发版扩展。

### 4. 铁壁防御：加密与区块链级存证 (Crypto & Hash Chain)
- **底层逻辑**：保障买卖双方的商业沟通绝对私密与防抵赖。
- **技术实现**：基于 WebSocket 直连，Node.js 侧强制使用 **AES-256-CBC** 对称加密所有聊天载荷。同时，利用上一条消息的哈希值计算本条消息的 **SHA-256 级联哈希**，构建不可篡改的消息证据链条。

---

## 💻 三、 全栈技术栈清单 (Technology Stack)

### 📱 客户端 (Android / Kotlin)
- **核心基座**: Kotlin, JDK 17, Target SDK 34, Min SDK 24。
- **UI & 动效**: Jetpack Compose, Material 3 动态语义色彩。
- **架构范式**: MVI 单向数据流 (StateFlow), MVVM, Dagger Hilt 依赖注入, Coroutines 并发。
- **网络与缓存**: Retrofit2, OkHttp3, Room DB (用于本地高并发聊天日志缓存)。

### 🌐 服务端 (Node.js / Express)
- **运行环境**: Node.js v20+, TypeScript 强类型约束, PM2 进程守护。
- **API 范式**: Express.js 提供高并发 RESTful API，挂载严格的 Auth 中间件与风控体系。
- **持久化层**: Prisma ORM, PostgreSQL (5432) 提供事务与 JSONB 高级查询支持。

### 🖥 后台管控台 (Admin-Web)
- **Web 栈**: React 18, Vite 构建, 纯 CSS3 手撸的类 M3 后台皮肤（不依赖沉重的组件库以保证极速响应）。

---

## 🛑 四、 系统红线与接手原则 (Red Lines)

> **接替本项目的下一代 Agent 或架构师，在编码时必须时刻默念以下三大红线：**

1. **绝对纯粹的撮合平台，严禁涉足电商闭环！**
   - 绝不允许在数据库中新建任何如 `ShoppingCart` (购物车) 或 `OrderDelivery` (物流发货) 的表结构。平台仅提供虚拟货币 (PC) 进行推广位购买。
2. **坚守原生轻量定位，严禁引入第三方 LBS 依赖！**
   - 不允许在 `build.gradle` 中引入任何臃肿的第三方地图包。
3. **M3 动态色彩纪律，严禁写死颜色！**
   - Compose 中不允许出现 `Color(0xFFFF0000)` 之类的硬编码。所有组件背景与字体颜色必须从 `MaterialTheme.colorScheme` (如 `primaryContainer`, `onSurfaceVariant`) 中提取，以支持系统级深浅色主题无缝切换。

---

## 🚀 五、 下一阶段二次开发核心任务 (Next Tasks)

新对话开启后，请优先从以下硬骨头着手推进：

1. **纯文本层级地址选择器 UI 落地 (Cascading Address Picker)**
   - **场景**：目前系统仅支持“一键GPS定位”。当定位失败时，用户需要手动选择乡镇。
   - **任务**：在 `PublishScreen` (发帖页)，实现一个基于纯本地数据的“省-市-县-镇”四级滑动/列表选择器（结合 M3 `ModalBottomSheet`）。纯文本驱动，无需网络和地图渲染。
2. **Prisma 数据库的线上演进 (Schema Sync)**
   - **任务**：随着 V1.0 业务的展开，需对线上 PostgreSQL 进行 `npx prisma db push` 操作，以安全地同步可能扩容的商家扩展字段，为后续的企业蓝V认证做准备。
3. **PowerShell 增量编译与防乱码规范 (Safe Build)**
   - **注意**：Android 编译时，必须使用命令 `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat clean assembleRelease ...`，严禁使用原生 PowerShell 直接做源码的字符替换（会引发 GBK/UTF-8 Mojibake 乱码灾难），务必使用 Python 脚本处理源码修改。
