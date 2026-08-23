$ErrorActionPreference = "Stop"

Write-Host "1. Building backend..."
cd d:\LsLife\backend
npm run build

Write-Host "2. Building admin-web..."
cd d:\LsLife\admin-web
npm run build

Write-Host "3. Packaging files..."
cd d:\LsLife
if (Test-Path deploy_tmp) { Remove-Item -Recurse -Force deploy_tmp }
New-Item -ItemType Directory -Path deploy_tmp | Out-Null

# Copy backend files (exclude node_modules and coverage)
& robocopy backend deploy_tmp\backend /E /XD node_modules coverage .git /XF .env
if ($LASTEXITCODE -ge 8) { throw "Robocopy failed" }

# Copy admin-web dist
& robocopy admin-web\dist deploy_tmp\admin-web-dist /E
if ($LASTEXITCODE -ge 8) { throw "Robocopy failed" }

# Replace sqlite with postgresql safely
node -e "const fs=require('fs');let c=fs.readFileSync('deploy_tmp/backend/prisma/schema.prisma','utf8');c=c.replace('provider = \`"sqlite\`"','provider = \`"postgresql\`"');fs.writeFileSync('deploy_tmp/backend/prisma/schema.prisma',c);"

# Create tar archive
cd deploy_tmp
tar -czf release.tgz backend admin-web-dist
if ($LASTEXITCODE -ne 0) { throw "tar failed" }

Write-Host "4. Uploading to remote server..."
scp release.tgz lslife@115.191.6.95:/home/lslife/release.tgz

Write-Host "5. Executing remote deployment script..."
ssh lslife@115.191.6.95 "
  export PATH=`"`$HOME/.local/nodejs/bin:`$PATH`"
  cd /home/lslife
  rm -rf release_tmp
  mkdir release_tmp
  tar -xzf release.tgz -C release_tmp
  
  echo 'Deploying backend...'
  cp -r release_tmp/backend/* /home/lslife/lslife-backend/
  cd /home/lslife/lslife-backend
  npm install --production
  npx prisma generate
  npx prisma db push --accept-data-loss
  pm2 restart all
  
  echo 'Deploying admin-web...'
  echo 'Maxence2468;' | sudo -S cp -r /home/lslife/release_tmp/admin-web-dist/* /var/www/html/admin-web/
  echo 'Maxence2468;' | sudo -S chmod -R 755 /var/www/html/admin-web/
  echo 'Deployment successful.'
"
Write-Host "Done!"
