#!/usr/bin/env bash
set -euo pipefail
export PATH="$HOME/.local/nodejs/bin:$PATH"
APP=/home/lslife/lslife-backend
cd "$APP"

mkdir -p public/uploads
cp -f upload_hotfix/app.ts src/app.ts
cp -f upload_hotfix/publish.ts src/modules/publish.ts
cp -f upload_hotfix/upload.ts src/modules/upload.ts
cp -f upload_hotfix/moderation.ts src/services/moderation.ts
cp -f upload_hotfix/smoke.ts scripts/smoke.ts
cp -f upload_hotfix/stress-publish.ts scripts/stress-publish.ts
cp -f upload_hotfix/package.json package.json

# PUBLIC_BASE_URL
if ! grep -q '^PUBLIC_BASE_URL=' .env; then
  echo 'PUBLIC_BASE_URL="https://mentalhlp.site"' >> .env
else
  sed -i 's|^PUBLIC_BASE_URL=.*|PUBLIC_BASE_URL="https://mentalhlp.site"|' .env
fi

# nginx uploads location (idempotent via python)
python3 - <<'PY'
from pathlib import Path
p = Path('/etc/nginx/sites-available/lslife')
if not p.exists():
    print('nginx conf missing, skip')
else:
    text = p.read_text()
    snippet = '''
    location /uploads/ {
        alias /home/lslife/lslife-backend/public/uploads/;
        expires 7d;
        add_header Cache-Control "public";
        access_log off;
    }
'''
    if 'location /uploads/' not in text:
        # insert before location /api/
        needle = 'location /api/'
        if needle in text:
            text = text.replace(needle, snippet + '\n    ' + needle, 1)
            # need root to write - try write to tmp and instruct
            Path('/tmp/lslife_nginx.conf').write_text(text)
            print('NGINX_PATCH_WRITTEN_/tmp')
        else:
            print('NGINX_NEEDLE_MISSING')
    else:
        print('NGINX_UPLOADS_ALREADY')
PY

if [ -f /tmp/lslife_nginx.conf ]; then
  if sudo -n true 2>/dev/null; then
    sudo cp /tmp/lslife_nginx.conf /etc/nginx/sites-available/lslife
    sudo nginx -t && sudo systemctl reload nginx
    echo NGINX_RELOADED
  else
    echo 'NGINX_NEEDS_SUDO: copy /tmp/lslife_nginx.conf manually'
  fi
fi

npm run build
pm2 delete lslife-api || true
pm2 start ecosystem.config.cjs
pm2 save
sleep 2

echo '==> smoke publish slice'
python3 - <<'PY'
import urllib.request, json, random
BASE='http://127.0.0.1:4000/api'
phone='138'+str(random.randint(10000000,99999999))
password='Pub'+str(random.randint(100000,999999))

def call(method, path, body=None, token=None):
  data=None if body is None else json.dumps(body).encode()
  req=urllib.request.Request(BASE+path, data=data, method=method)
  req.add_header('Content-Type','application/json')
  if token: req.add_header('Authorization','Bearer '+token)
  with urllib.request.urlopen(req, timeout=20) as r:
    j=json.loads(r.read().decode())
  if j['code']!=0: raise SystemExit(j)
  return j['data']

reg=call('POST','/auth/register',{'phone':phone,'password':password})
token=reg['token']
post=call('POST','/posts',{
  'category':'second_hand',
  'description':'九成新台灯，连山自提，功能完好。',
  'price':35,
  'images':['https://images.unsplash.com/photo-1485965120186-bdfc4c6e3e1b?w=400'],
  'brand':'其他','condition':'几乎全新','shipping':'自提','locationName':'连山吉田镇'
}, token)
assert post['status']=='published', post
feed=call('GET','/posts?category=second_hand')
assert any(p['id']==post['id'] for p in feed['list']), feed
print('PUBLISH_E2E_OK', post['id'])
PY

echo '==> stress'
BASE=http://127.0.0.1:4000/api CONCURRENCY=8 ROUNDS=2 npx tsx scripts/stress-publish.ts
