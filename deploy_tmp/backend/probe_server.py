import paramiko

BACKEND  = '/home/lslife/backend'
NODE_BIN = '/home/lslife/.local/nodejs/bin'

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('115.191.6.95', 22, 'lslife',
          key_filename=r'C:\Users\xl246\.ssh\id_lslife', timeout=20)

def r(cmd, label=''):
    full = f'export PATH="{NODE_BIN}:$PATH" && {cmd}'
    _, o, e = c.exec_command(full)
    o.channel.recv_exit_status()
    out = o.read().decode(errors='replace').strip()
    if out: print(f'  [{label}]\n  {out[:800]}')
    return out

print('[1] Check ai.ts source on server:')
r(f'grep -n "apiKey\\|env.aiApiKey\\|hardcode" {BACKEND}/src/services/ai.ts | head -10', 'ai.ts grep')

print('\n[2] Check dist/services/ai.js on server:')
r(f'grep -n "sk-30f79d21\\|env.aiApiKey\\|AI_API_KEY" {BACKEND}/dist/services/ai.js 2>/dev/null | head -10', 'dist grep')

print('\n[3] Does dist exist and when was it built:')
r(f'ls -la {BACKEND}/dist/services/ 2>/dev/null | head -5', 'dist ls')
r(f'stat {BACKEND}/dist/services/ai.js 2>/dev/null', 'ai.js stat')

print('\n[4] Check running process:')
r('ps aux | grep "node.*index" | grep -v grep', 'node process')

print('\n[5] Check server .env AI vars:')
r(f'grep "^AI_" {BACKEND}/.env', 'AI env vars')

c.close()
print('\n=== VERIFY DONE ===')
