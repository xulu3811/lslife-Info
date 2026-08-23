#!/usr/bin/env pwsh
# deploy_ai_fix.ps1 — 将 AI Key 修复推送到生产服务器
# 使用方式 (在 D:\LsLife\backend 下执行): .\deploy_ai_fix.ps1

$SERVER  = "lslife@115.191.6.95"
$REMOTE  = "~/lslife-backend"

Write-Host "=== LsLife AI Key Fix Deployment ===" -ForegroundColor Magenta

Write-Host "[1/3] 上传修改后的 ai.ts ..." -ForegroundColor Cyan
scp -o StrictHostKeyChecking=no `
    "$PSScriptRoot\src\services\ai.ts" `
    "${SERVER}:${REMOTE}/src/services/ai.ts"
if ($LASTEXITCODE -ne 0) { Write-Error "SCP 上传失败！"; exit 1 }

Write-Host "[2/3] 更新服务器 .env ..." -ForegroundColor Cyan
$envScript = @'
cd ~/lslife-backend
cp .env .env.bak-$(date +%Y%m%d%H%M%S) 2>/dev/null

# 更新或添加 AI_PROVIDER
if grep -q '^AI_PROVIDER=' .env; then
  sed -i 's|^AI_PROVIDER=.*|AI_PROVIDER="deepseek"|' .env
else
  echo 'AI_PROVIDER="deepseek"' >> .env
fi

# 更新或添加 AI_API_KEY
if grep -q '^AI_API_KEY=' .env; then
  sed -i 's|^AI_API_KEY=.*|AI_API_KEY="sk-30f79d21acbd487da71ec3cb5ce63d54"|' .env
else
  echo 'AI_API_KEY="sk-30f79d21acbd487da71ec3cb5ce63d54"' >> .env
fi

# 更新或添加 AI_MODEL
if grep -q '^AI_MODEL=' .env; then
  sed -i 's|^AI_MODEL=.*|AI_MODEL="deepseek-chat"|' .env
else
  echo 'AI_MODEL="deepseek-chat"' >> .env
fi

echo "--- 当前 AI 配置 ---"
grep -E '^AI_' .env
'@
ssh -o StrictHostKeyChecking=no $SERVER $envScript
if ($LASTEXITCODE -ne 0) { Write-Error "SSH 配置失败！"; exit 1 }

Write-Host "[3/3] 重新构建 TypeScript 并重启 PM2 ..." -ForegroundColor Cyan
$buildScript = @'
cd ~/lslife-backend
npm run build 2>&1 | tail -10
pm2 restart lslife-backend --update-env
sleep 2
pm2 show lslife-backend | grep -E "status|restart|uptime"
echo "✅ 部署完成"
'@
ssh -o StrictHostKeyChecking=no $SERVER $buildScript

Write-Host "🎉 AI Key 修复已部署到生产服务器！" -ForegroundColor Green
