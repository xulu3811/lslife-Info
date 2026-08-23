#!/usr/bin/env bash
# 需要 root: sudo bash ~/lslife-backend/deploy/setup-production.sh
# 完成: Nginx + Certbot + PostgreSQL + 防火墙放行 80/443 + PM2 开机自启
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "请使用: sudo bash $0" >&2
  exit 1
fi

APP_USER="lslife"
APP_HOME="/home/${APP_USER}"
APP_DIR="${APP_HOME}/lslife-backend"
DOMAIN="mentalhlp.site"
EMAIL="admin@${DOMAIN}"

export DEBIAN_FRONTEND=noninteractive

echo "==> [1/6] 安装 Nginx / Certbot / PostgreSQL ..."
apt-get update -y
apt-get install -y nginx certbot python3-certbot-nginx postgresql postgresql-contrib ufw

echo "==> [2/6] 配置 UFW (仅开放 SSH/80/443, 不对外暴露 4000/5432) ..."
ufw allow OpenSSH || true
ufw allow 80/tcp || true
ufw allow 443/tcp || true
ufw --force enable || true

echo "==> [3/6] 配置 PostgreSQL ..."
# 生成强密码并写入应用 .env (若已有 PG 密码则复用)
ENV_FILE="${APP_DIR}/.env"
if [ -f "${ENV_FILE}" ] && grep -q '^PG_PASSWORD=' "${ENV_FILE}"; then
  # shellcheck disable=SC1090
  PG_PASSWORD="$(grep '^PG_PASSWORD=' "${ENV_FILE}" | cut -d= -f2- | tr -d '"')"
else
  PG_PASSWORD="$(openssl rand -hex 16)"
fi

sudo -u postgres psql -tc "SELECT 1 FROM pg_roles WHERE rolname='lslife'" | grep -q 1 \
  || sudo -u postgres psql -c "CREATE USER lslife WITH PASSWORD '${PG_PASSWORD}';"
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname='lslife'" | grep -q 1 \
  || sudo -u postgres psql -c "CREATE DATABASE lslife OWNER lslife;"
sudo -u postgres psql -c "ALTER USER lslife WITH PASSWORD '${PG_PASSWORD}';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE lslife TO lslife;"
# Postgres 15+ 需要 schema 权限
sudo -u postgres psql -d lslife -c "GRANT ALL ON SCHEMA public TO lslife;"
sudo -u postgres psql -d lslife -c "ALTER SCHEMA public OWNER TO lslife;"

# 仅本机访问
PG_HBA="/etc/postgresql/*/main/pg_hba.conf"
# 确保 local/peer 与 127.0.0.1 md5 存在 (默认已够用)

echo "==> [4/6] 写入/更新应用 .env (PostgreSQL) ..."
JWT_SECRET="$(grep '^JWT_SECRET=' "${ENV_FILE}" 2>/dev/null | cut -d= -f2- | tr -d '"' || true)"
if [ -z "${JWT_SECRET}" ]; then
  JWT_SECRET="$(openssl rand -hex 32)"
fi

cat > "${ENV_FILE}" <<EOF
PORT=4000
DATABASE_URL="postgresql://lslife:${PG_PASSWORD}@127.0.0.1:5432/lslife?schema=public"
PG_PASSWORD="${PG_PASSWORD}"
JWT_SECRET="${JWT_SECRET}"
JWT_EXPIRES_IN="30d"
SMS_PROVIDER="mock"
PAY_PROVIDER="mock"
AI_PROVIDER="mock"
CONTENT_MODERATION_ENABLED="true"
NODE_ENV="production"
EOF
chown "${APP_USER}:${APP_USER}" "${ENV_FILE}"
chmod 600 "${ENV_FILE}"

echo "==> [5/6] 配置 Nginx 反代 ..."
mkdir -p /var/www/certbot
cp "${APP_DIR}/deploy/nginx-lslife.conf" /etc/nginx/sites-available/lslife
ln -sfn /etc/nginx/sites-available/lslife /etc/nginx/sites-enabled/lslife
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl enable nginx
systemctl restart nginx

echo "==> [6/6] PM2 开机自启 ..."
NODE_BIN="${APP_HOME}/.local/nodejs/bin"
# 使用 oneshot+RemainAfterExit, 避免依赖不存在的 pm2.pid (Type=forking 会失败)
cat > /etc/systemd/system/pm2-${APP_USER}.service <<EOF
[Unit]
Description=PM2 process manager for ${APP_USER}
Documentation=https://pm2.keymetrics.io/
After=network.target postgresql.service

[Service]
Type=oneshot
RemainAfterExit=yes
User=${APP_USER}
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
Environment=PATH=${NODE_BIN}:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
Environment=PM2_HOME=${APP_HOME}/.pm2
Restart=on-failure

ExecStart=${NODE_BIN}/pm2 resurrect
ExecReload=${NODE_BIN}/pm2 reload all
ExecStop=${NODE_BIN}/pm2 kill

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable "pm2-${APP_USER}.service"
# 若 PM2 已在跑, start 会成功并把 unit 标为 active
systemctl reset-failed "pm2-${APP_USER}.service" || true
systemctl start "pm2-${APP_USER}.service" || true

# 尝试签发证书 (DNS 必须已指向本机公网 IP)
PUBLIC_IP="$(curl -fsS -m 8 https://api.ipify.org || curl -fsS -m 8 ifconfig.me || echo '')"
DNS_IP="$(getent ahostsv4 ${DOMAIN} | awk '{print $1; exit}')"
echo ""
echo "本机公网 IP: ${PUBLIC_IP:-未知}"
echo "域名 ${DOMAIN} 当前解析: ${DNS_IP:-未知}"

if [ -n "${PUBLIC_IP}" ] && [ "${DNS_IP}" = "${PUBLIC_IP}" ]; then
  echo "==> DNS 已正确, 签发 Let's Encrypt 证书 ..."
  certbot --nginx -d "${DOMAIN}" -d "www.${DOMAIN}" \
    --non-interactive --agree-tos -m "${EMAIL}" --redirect || {
      echo "证书签发失败, 可稍后手动: certbot --nginx -d ${DOMAIN} -d www.${DOMAIN}"
    }
else
  echo "==> [跳过证书] 请先把 ${DOMAIN} / www.${DOMAIN} 的 A 记录改为 ${PUBLIC_IP:-本机公网IP}"
  echo "    修改生效后执行:"
  echo "    sudo certbot --nginx -d ${DOMAIN} -d www.${DOMAIN} --non-interactive --agree-tos -m ${EMAIL} --redirect"
fi

echo ""
echo "基础环境安装完成。接下来请以 ${APP_USER} 用户执行:"
echo "  bash ${APP_DIR}/deploy/migrate-to-postgres.sh"
echo ""
