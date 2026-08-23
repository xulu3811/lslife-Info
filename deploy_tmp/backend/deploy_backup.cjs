const { NodeSSH } = require('node-ssh');
const fs = require('fs');
const path = require('path');

const prodSsh = new NodeSSH();
const backupSsh = new NodeSSH();

async function run() {
  try {
    console.log('Connecting to Backup Server (47.107.179.201)...');
    await backupSsh.connect({
      host: '47.107.179.201',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected to Backup Server.');

    // Phase 1: Upload Source Code to Backup Server
    console.log('Ensuring /root/backups exists...');
    await backupSsh.execCommand('mkdir -p /root/backups');
    
    console.log('Uploading D:\\LsLife_V2.01_SourceCode.tar.gz to Backup Server...');
    const localFile = 'D:\\LsLife_V2.01_SourceCode.tar.gz';
    if (fs.existsSync(localFile)) {
      await backupSsh.putFile(localFile, '/root/backups/LsLife_V2.01_SourceCode.tar.gz');
      console.log('Upload complete.');
    } else {
      console.log('Source Code archive not found, skipping Phase 1.');
    }
    
    backupSsh.dispose();

    // Phase 2: Configure Cron Backup on Production Server
    console.log('\nConnecting to Production Server (115.191.6.95)...');
    await prodSsh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });
    console.log('Connected to Production Server.');

    // Install sshpass if not exists
    console.log('Installing sshpass on Production Server...');
    await prodSsh.execCommand('apt-get update && apt-get install -y sshpass');

    // Create backup script
    const backupScriptContent = `#!/bin/bash
# Backup script for LsLife database and static files

BACKUP_DIR="/root/backups_local"
REMOTE_BACKUP_SERVER="47.107.179.201"
REMOTE_USER="root"
REMOTE_PASS="Maxence2468;"
REMOTE_DIR="/root/backups/lslife_prod"

DATE=$(date +"%Y%m%d_%H%M%S")
DB_BACKUP_FILE="db_backup_$DATE.sql.gz"
UPLOADS_BACKUP_FILE="uploads_backup_$DATE.tar.gz"

mkdir -p $BACKUP_DIR

# Dump Database
echo "Dumping database..."
# Assuming postgres is running in docker with user lslife, db lslife, password af4a98b163543c58c46bf827bdd546a8
PGPASSWORD="af4a98b163543c58c46bf827bdd546a8" pg_dump -h 127.0.0.1 -p 5433 -U lslife -d lslife | gzip > "$BACKUP_DIR/$DB_BACKUP_FILE"

# Archive uploads directory
echo "Archiving uploads..."
tar -czf "$BACKUP_DIR/$UPLOADS_BACKUP_FILE" -C /home/lslife/backend uploads/

# Transfer to Backup Server
echo "Transferring to Backup Server..."
sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no "$REMOTE_USER@$REMOTE_BACKUP_SERVER" "mkdir -p $REMOTE_DIR"
sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no "$BACKUP_DIR/$DB_BACKUP_FILE" "$BACKUP_DIR/$UPLOADS_BACKUP_FILE" "$REMOTE_USER@$REMOTE_BACKUP_SERVER:$REMOTE_DIR/"

# Cleanup local backups older than 7 days
find $BACKUP_DIR -type f -mtime +7 -delete

# Cleanup remote backups older than 7 days
sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no "$REMOTE_USER@$REMOTE_BACKUP_SERVER" "find $REMOTE_DIR -type f -mtime +7 -delete"

echo "Backup complete!"
`;

    console.log('Writing backup script to /root/backup_to_remote.sh...');
    await prodSsh.execCommand('cat << \'EOF\' > /root/backup_to_remote.sh\n' + backupScriptContent + '\nEOF');
    await prodSsh.execCommand('chmod +x /root/backup_to_remote.sh');

    console.log('Configuring Cron job...');
    const cronJob = '0 3 * * * /root/backup_to_remote.sh >> /var/log/lslife_backup.log 2>&1';
    await prodSsh.execCommand(`(crontab -l 2>/dev/null | grep -v "backup_to_remote.sh"; echo "${cronJob}") | crontab -`);

    console.log('\nRunning the backup script manually once for verification...');
    const backupResult = await prodSsh.execCommand('/root/backup_to_remote.sh');
    console.log('Backup Result:', backupResult.stdout);
    if (backupResult.stderr) {
      console.error('Backup Stderr:', backupResult.stderr);
    }

    prodSsh.dispose();
    console.log('\nAll operations completed successfully.');
  } catch (err) {
    console.error('An error occurred:', err);
    backupSsh.dispose();
    prodSsh.dispose();
  }
}

run();
