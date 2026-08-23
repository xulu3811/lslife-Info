import paramiko

NODE_BIN   = '/home/lslife/.local/nodejs/bin'
BACKEND    = '/home/lslife/backend'
BACKUP_DIR = '/home/lslife/backups'

DB_CONTAINER = 'lslife-backend_db_1'
DB_USER      = 'lslife'
DB_NAME      = 'lslife'
DB_PASS      = 'af4a98b163543c58c46bf827bdd546a8'

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('115.191.6.95', 22, 'lslife',
          key_filename=r'C:\Users\xl246\.ssh\id_lslife', timeout=20)
print('[AUTH] OK via pubkey')

def r(cmd, label='', show=True):
    _, o, e = c.exec_command(cmd)
    rc = o.channel.recv_exit_status()
    out = o.read().decode(errors='replace').strip()
    err = e.read().decode(errors='replace').strip()
    if show:
        tag = label or cmd[:55]
        if out: print(f'  [{tag}]\n  {out[:800]}')
        if err and rc != 0: print(f'  [ERR:{tag}] {err[:400]}')
    return out, rc

backup_script = f'''#!/bin/bash
# =============================================================
# LsLife 生产环境自动化备份脚本 v1.1
# 每日 03:00 自动执行（cron 驱动）
# 备份内容：PostgreSQL 全量 SQL dump + public/uploads 静态资源
# 保留策略：日备份 7 天 / 周备份（每周日）4 份
# =============================================================
set -eo pipefail

BACKUP_BASE="/home/lslife/backups"
BACKEND_DIR="/home/lslife/backend"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
WEEKDAY=$(date +%u)    # 1=周一 ... 7=周日
LOG_FILE="$BACKUP_BASE/logs/backup_$TIMESTAMP.log"

# PostgreSQL 连接参数
DB_CONTAINER="{DB_CONTAINER}"
DB_USER="{DB_USER}"
DB_NAME="{DB_NAME}"
DB_PASS="{DB_PASS}"

# 确保目录存在
mkdir -p "$BACKUP_BASE/daily" "$BACKUP_BASE/weekly" "$BACKUP_BASE/logs"

echo "[$(date '+%F %T')] ===== LsLife Backup Start =====" | tee -a "$LOG_FILE"

# ── Step 1: PostgreSQL dump ──────────────────────────────────
echo "[$(date '+%F %T')] Step1: pg_dump start ..." | tee -a "$LOG_FILE"

DB_DUMP="$BACKUP_BASE/daily/db_$TIMESTAMP.sql.gz"

docker exec "$DB_CONTAINER" \\
    env PGPASSWORD="$DB_PASS" \\
    pg_dump -U "$DB_USER" "$DB_NAME" \\
    | gzip > "$DB_DUMP"

echo "[$(date '+%F %T')] DB dump OK: $(du -sh $DB_DUMP | cut -f1)" | tee -a "$LOG_FILE"

# ── Step 2: uploads 静态资源 ──────────────────────────────────
echo "[$(date '+%F %T')] Step2: uploads backup ..." | tee -a "$LOG_FILE"

UPLOADS_SRC="$BACKEND_DIR/public/uploads"
UPLOADS_DUMP="$BACKUP_BASE/daily/uploads_$TIMESTAMP.tar.gz"

if [ -d "$UPLOADS_SRC" ]; then
    tar -czf "$UPLOADS_DUMP" -C "$BACKEND_DIR/public" uploads
    echo "[$(date '+%F %T')] Uploads OK: $(du -sh $UPLOADS_DUMP | cut -f1)" | tee -a "$LOG_FILE"
else
    echo "[$(date '+%F %T')] WARN: $UPLOADS_SRC not found, skipped" | tee -a "$LOG_FILE"
fi

# ── Step 3: 周备份（每周日额外保留一份）─────────────────────
if [ "$WEEKDAY" = "7" ]; then
    echo "[$(date '+%F %T')] Step3: creating weekly snapshot ..." | tee -a "$LOG_FILE"
    cp "$DB_DUMP" "$BACKUP_BASE/weekly/db_weekly_$TIMESTAMP.sql.gz"
    [ -f "$UPLOADS_DUMP" ] && \\
        cp "$UPLOADS_DUMP" "$BACKUP_BASE/weekly/uploads_weekly_$TIMESTAMP.tar.gz"
    echo "[$(date '+%F %T')] Weekly snapshot done" | tee -a "$LOG_FILE"
fi

# ── Step 4: 清理旧备份 ───────────────────────────────────────
echo "[$(date '+%F %T')] Step4: cleanup ..." | tee -a "$LOG_FILE"

# 日备份：按时间排序，保留最新 14 个文件（db+uploads 各 7 天）
find "$BACKUP_BASE/daily" -maxdepth 1 -name "*.gz" -type f | sort -r | tail -n +15 | xargs -r rm -f || true
# 周备份：保留最新 8 个文件（db+uploads 各 4 周）
find "$BACKUP_BASE/weekly" -maxdepth 1 -name "*.gz" -type f | sort -r | tail -n +9 | xargs -r rm -f || true
# 日志：保留最新 30 份
find "$BACKUP_BASE/logs" -maxdepth 1 -name "*.log" -type f | sort -r | tail -n +31 | xargs -r rm -f || true

# ── Step 5: 汇总报告 ─────────────────────────────────────────
TOTAL=$(du -sh "$BACKUP_BASE" 2>/dev/null | cut -f1)
DAILY_COUNT=$(ls "$BACKUP_BASE/daily/" 2>/dev/null | wc -l)
WEEKLY_COUNT=$(ls "$BACKUP_BASE/weekly/" 2>/dev/null | wc -l)
echo "[$(date '+%F %T')] ===== Backup Complete =====" | tee -a "$LOG_FILE"
echo "[$(date '+%F %T')] Total: $TOTAL | Daily: $DAILY_COUNT files | Weekly: $WEEKLY_COUNT files" | tee -a "$LOG_FILE"
'''

sftp = c.open_sftp()
with sftp.open('/home/lslife/lslife_backup.sh', 'w') as f:
    f.write(backup_script)
sftp.close()

r('chmod +x /home/lslife/lslife_backup.sh', 'chmod +x')

print('\n[Testing fixed backup script...]')
out, rc = r('bash /home/lslife/lslife_backup.sh 2>&1', 'test run')
if rc == 0:
    print('  ✅ 手动测试运行完全成功！')
else:
    print(f'  ❌ 运行失败 rc={rc}')

c.close()
