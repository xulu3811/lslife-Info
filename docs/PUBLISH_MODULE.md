# 发布模块二次开发说明（闲鱼对标 · 已打通）

> 日期：2026-07-20

## 原问题根因

1. Android 强制校验 `title`，但 UI 无标题输入 → 永远提示「请填写标题和描述」
2. 审核策略一律 `pending_review` → 信息流看不到帖子
3. 首页分类 `secondhand` ≠ 后端 `second_hand`，且首页只拉商家不拉帖子
4. 上传目录 / HTTPS 公网 URL 不稳定

## 已实现（闲鱼式链路）

```
选图(1-9) → 描述/可选标题 → 分类属性 → 价格 → 发货方式 → 位置
  → 并发上传 /api/upload → POST /api/posts 入库
  → status=published 进入 GET /api/posts 信息流
  → 首页「个人闲置」等 UGC 分类展示帖子
  → 「发布」页展示「我的发布」
```

## 压测结果（生产本机 API）

- `CONCURRENCY=8 ROUNDS=2` → **16/16 成功**
- 延迟 avg≈929ms · p95≈989ms
- 公网信息流：`GET https://mentalhlp.site/api/posts?category=second_hand` 可见

## 命令

```bash
cd backend
npm run smoke
BASE=https://mentalhlp.site/api CONCURRENCY=10 ROUNDS=3 npm run stress:publish
```
