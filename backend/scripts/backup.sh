#!/bin/bash
set -e

# Load .env to get DATABASE_URL
# Format is key=value. This ignores lines starting with #.
export $(grep -v '^#' /home/lslife/lslife-backend/.env | xargs)

BACKUP_DIR="/home/lslife/backups"
APP_DIR="/home/lslife/lslife-backend"
DATE=$(date +"%Y%m%d_%H%M%S")

echo "Starting backup process at $(date)..."

# 1. Backup PostgreSQL Database
DB_BACKUP_FILE="$BACKUP_DIR/db_$DATE.sql"
echo "Dumping database to $DB_BACKUP_FILE..."
# pg_dump requires connection string. We can pass DATABASE_URL directly if it is a valid postgres string.
pg_dump "$DATABASE_URL" -F p -f "$DB_BACKUP_FILE"

# Compress DB backup
gzip "$DB_BACKUP_FILE"
echo "Database backed up successfully."

# 2. Backup Static Files
STATIC_BACKUP_FILE="$BACKUP_DIR/static_$DATE.tar.gz"
echo "Archiving static files to $STATIC_BACKUP_FILE..."
tar -czf "$STATIC_BACKUP_FILE" -C "$APP_DIR" public/uploads public/assets
echo "Static files archived successfully."

# 3. Cleanup old backups (older than 7 days)
echo "Cleaning up backups older than 7 days..."
find "$BACKUP_DIR" -type f -name "db_*.sql.gz" -mtime +7 -exec rm {} \;
find "$BACKUP_DIR" -type f -name "static_*.tar.gz" -mtime +7 -exec rm {} \;

echo "Backup process completed successfully at $(date)."
