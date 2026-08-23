import paramiko, time

BACKEND  = '/home/lslife/backend'
NODE_BIN = '/home/lslife/.local/nodejs/bin'
DIST_AI  = f'{BACKEND}/dist/services/ai.js'

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('115.191.6.95', 22, 'lslife',
          key_filename=r'C:\Users\xl246\.ssh\id_lslife', timeout=20)
print('[AUTH] OK')

def r(cmd, label=''):
    full = f'export PATH="{NODE_BIN}:$PATH" && {cmd}'
    _, o, e = c.exec_command(full)
    rc = o.channel.recv_exit_status()
    out = o.read().decode(errors='replace').strip()
    err = e.read().decode(errors='replace').strip()
    if out: print(f'  [{label or cmd[:40]}] {out[:600]}')
    if err and rc != 0: print(f'  [ERR] {err[:300]}')
    return out, rc

# 1. 查看 dist/ai.js 中硬编码 Key 的上下文
print('\n[1] Current dist/ai.js around line 74:')
r(f'sed -n "68,85p" {DIST_AI}', 'dist context')

# 2. 先尝试安装 tsc 全局并重新编译
print('\n[2] Try to install typescript and rebuild ...')
r(f'cd {BACKEND} && npm install --save-dev typescript@latest 2>&1 | tail -3', 'npm install tsc')
out, rc = r(f'cd {BACKEND} && ./node_modules/.bin/tsc -p tsconfig.json 2>&1 | tail -8', 'tsc build')
build_ok = (rc == 0)
print(f'  Build exit code: {rc}, success: {build_ok}')

if build_ok:
    print('\n[3] Build succeeded! Verifying new dist ...')
    r(f'grep -n "sk-30f79d21\\|env.aiApiKey" {DIST_AI} | head -10', 'new dist check')
else:
    # 3. Fallback: 直接 sed 替换 dist 中的硬编码 Key
    print('\n[3] Build failed. Directly patching dist/ai.js ...')
    
    # 备份
    r(f'cp {DIST_AI} {DIST_AI}.bak', 'backup dist')
    
    # 查看硬编码行的完整内容
    out_line, _ = r(f'grep -n "sk-30f79d21" {DIST_AI}', 'find hardcode line')
    print(f'  Found hardcoded line: {out_line}')
    
    # 策略：将 const apiKey = '...' 替换为 const apiKey = env_1.env.aiApiKey
    # 先确认编译后 env 模块的引用名
    r(f'grep -n "env_1\\|env\\.aiApiKey\\|require.*env" {DIST_AI} | head -5', 'env ref in dist')
    
    # 替换 hardcoded key 行
    patch_cmd = (
        f"sed -i "
        f"\"s/const apiKey = 'sk-30f79d21acbd487da71ec3cb5ce63d54';"
        f"/const apiKey = env_1.env.aiApiKey;/\" "
        f"{DIST_AI}"
    )
    r(patch_cmd, 'patch dist apiKey')
    
    # 同时替换 'deepseek-chat' 为 env_1.env.aiModel
    patch_model = (
        f"sed -i "
        f"\"s/model: 'deepseek-chat',/model: env_1.env.aiModel || 'deepseek-chat',/\" "
        f"{DIST_AI}"
    )
    r(patch_model, 'patch dist model')
    
    # 验证
    r(f'grep -n "apiKey\\|deepseek-chat\\|sk-30f79d21" {DIST_AI} | head -10', 'verify patch')

# 4. Restart PM2
print('\n[4] Restart PM2 ...')
r('pm2 restart all --update-env 2>&1', 'pm2 restart')
time.sleep(3)
r('pm2 list 2>&1 | grep -E "name|online|errored"', 'pm2 status')

# 5. Final health check
print('\n[5] Final health check ...')
r('curl -s https://mentalhlp.site/api/health', 'health check')

c.close()
print('\n=== PATCH COMPLETE ===')
