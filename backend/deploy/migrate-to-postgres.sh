#!/usr/bin/env bash
# 以 lslife 用户执行: 切换 Prisma 到 PostgreSQL → 建表 → 种子 → 重启 PM2
set -euo pipefail

APP_DIR="$HOME/lslife-backend"
export PATH="$HOME/.local/nodejs/bin:$PATH"
export PRISMA_ENGINES_MIRROR="https://registry.npmmirror.com/-/binary/prisma"

cd "$APP_DIR"

if [ ! -f .env ]; then
  echo "缺少 .env, 请先 sudo bash deploy/setup-production.sh" >&2
  exit 1
fi

# shellcheck disable=SC1091
set -a; source .env; set +a

if [[ "${DATABASE_URL}" != postgresql* ]]; then
  echo "DATABASE_URL 不是 postgresql, 当前: ${DATABASE_URL}" >&2
  exit 1
fi

echo "==> 切换 Prisma provider → postgresql ..."
# 备份并替换 (幂等)
cp -n prisma/schema.prisma "prisma/schema.prisma.bak.sqlite" 2>/dev/null || true
sed -i 's/provider = "sqlite"/provider = "postgresql"/' prisma/schema.prisma
grep -n 'provider =' prisma/schema.prisma | head -5

echo "==> Prisma generate + db push ..."
npx prisma generate
npx prisma db push --skip-generate --accept-data-loss

echo "==> 导入种子数据 ..."
npm run seed

echo "==> 重新构建并重启 PM2 ..."
npm run build
pm2 delete lslife-api >/dev/null 2>&1 || true
pm2 start dist/index.js --name lslife-api --time
pm2 save

sleep 2
echo "==> 本机健康检查:"
curl -fsS http://127.0.0.1:4000/api/health
echo
curl -fsS "http://127.0.0.1:4000/api/merchants?limit=1" | head -c 200
echo
echo ""
echo "迁移完成。若 DNS/证书已就绪, 外网验证:"
echo "  curl -fsS https://mentalhlp.site/api/health"
