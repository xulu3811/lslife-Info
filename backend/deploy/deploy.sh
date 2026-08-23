#!/usr/bin/env bash
# 在服务器上完成后端安装/初始化/构建/启动 (无 sudo)
set -euo pipefail

APP_DIR="$HOME/lslife-backend"
export PATH="$HOME/.local/nodejs/bin:$PATH"
export PRISMA_ENGINES_MIRROR="https://registry.npmmirror.com/-/binary/prisma"

cd "$APP_DIR"

echo "==> Node: $(node -v), npm: $(npm -v)"

# 生产环境变量 (仅首次创建, 保留已有密钥)
if [ ! -f .env ]; then
  JWT=$(node -e "console.log(require('crypto').randomBytes(32).toString('hex'))")
  cat > .env <<EOF
PORT=4000
DATABASE_URL="file:./prod.db"
JWT_SECRET="${JWT}"
JWT_EXPIRES_IN="30d"
SMS_PROVIDER="mock"
PAY_PROVIDER="mock"
AI_PROVIDER="mock"
CONTENT_MODERATION_ENABLED="true"
NODE_ENV="production"
EOF
  echo "==> 已生成 .env (随机 JWT_SECRET)"
fi

echo "==> 安装依赖..."
npm install --registry=https://registry.npmmirror.com --no-audit --no-fund

echo "==> Prisma 生成与建表..."
npx prisma generate
npx prisma db push --skip-generate

echo "==> 导入种子数据..."
npm run seed

echo "==> 构建..."
npm run build

echo "==> 安装 PM2 (如缺失)..."
command -v pm2 >/dev/null 2>&1 || npm install -g pm2 --registry=https://registry.npmmirror.com

echo "==> 启动/重载服务..."
pm2 delete lslife-api >/dev/null 2>&1 || true
pm2 start dist/index.js --name lslife-api --time
pm2 save

echo "==> 部署完成。当前进程:"
pm2 status
