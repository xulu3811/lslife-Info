# 连山同城 LsLife · Android 原生客户端

Kotlin + Jetpack Compose + Material 3 实现的同城生活服务用户端 App，对接 `../backend` 后端。

## 技术栈
- 语言/UI: Kotlin 2.0 / Jetpack Compose / Material 3
- 架构: MVVM + 单向数据流（StateFlow）
- DI: Hilt
- 网络: Retrofit + OkHttp + Kotlinx Serialization
- 实时: OkHttp WebSocket (`RealtimeClient`)
- 本地: Room（商家离线缓存）+ DataStore（登录态）
- 图片: Coil

## 环境要求
- Android Studio Ladybug 以上
- JDK 17（当前机器默认 JDK 8，构建前请切换到 17）
- Android SDK / compileSdk 35

## 构建运行
```bash
cd android
# 首次: 生成 Gradle Wrapper (仓库未附带二进制 wrapper jar)
gradle wrapper --gradle-version 8.11.1
./gradlew assembleDebug
```
- 后端地址：`app/build.gradle.kts` 中 `API_BASE_URL` / `WS_BASE_URL`
  - 生产：`https://mentalhlp.site/api/` · WebSocket `wss://mentalhlp.site/ws`
  - 本地模拟器临时调试：改为 `http://10.0.2.2:4000/api/` · `ws://10.0.2.2:4000/ws`
- 登录：mock 短信环境下点击“获取验证码”会自动回填验证码，直接登录体验完整链路。

## 目录结构（包分层，可平滑拆分为 Gradle 多模块）
```
com.lianshan.lslife/
  LsLifeApplication.kt / MainActivity.kt
  di/            NetworkModule, DatabaseModule
  core/
    model/       序列化数据模型
    network/     ApiService, Requests, AuthInterceptor, RealtimeClient, Safe
    data/        TokenStore, AuthRepository, LsRepository
    database/    Room: AppDatabase, MerchantDao, Entities
  ui/
    theme/       颜色/主题（复刻 #FF1A1A）
    navigation/  Routes
    components/   通用组件
    LsLifeApp.kt 底部导航 + NavHost
  feature/
    auth/        登录（短信验证码）
    home/        首页（商家列表/搜索/分类/排序/推荐）
    merchant/    商家详情 + 加购 + 结算下单支付
    cart/        购物车（服务端同步）
    orders/      订单列表 + 实时配送追踪
    publish/     同城发布 + 会员额度
    profile/     个人中心 + 会员订阅 + 退出登录
```

## 已实现链路
登录/注册 → 首页浏览/搜索 → 商家详情加购 → 结算（下单+支付+清购物车）→ 订单实时追踪（轮询/可切 WebSocket）→ 同城发布（额度校验）→ 会员升级。

## 生产化 TODO（需外部资质/密钥）
- 地图/定位：接入高德或百度地图 Android SDK 替换 `OrderTrackScreen` 的简易路线图（需申请 Key、SHA1）。
- 支付：接入微信支付/支付宝官方 SDK，用后端 `payments/create` 返回的 prepay 参数拉起原生收银台。
- 推送：接入华为/小米/OPPO/vivo 厂商通道 + 极光/个推兜底。
- 崩溃/埋点：Bugly 或 Firebase Crashlytics。
- 多渠道打包与各应用市场上架（见 `../docs/COMPLIANCE.md`）。
