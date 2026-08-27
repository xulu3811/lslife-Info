import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;'
    });

    console.log('--- Executing Schema Upgrade ---');
    const schemaCmds = `
BEGIN;
ALTER TABLE "Post" ALTER COLUMN attributes DROP DEFAULT;
ALTER TABLE "Post" ALTER COLUMN attributes TYPE jsonb USING attributes::jsonb;
ALTER TABLE "Post" ALTER COLUMN attributes SET DEFAULT '{}'::jsonb;
CREATE INDEX IF NOT EXISTS idx_post_attributes ON "Post" USING GIN (attributes);
COMMIT;
    `;
    
    await ssh.execCommand(`cat << 'EOF' > /tmp/upgrade.sql\n${schemaCmds}\nEOF`);
    
    const res = await ssh.execCommand(`cat /tmp/upgrade.sql | docker exec -i lslife-backend_db_1 psql -U lslife -d lslife`);
    console.log('Schema upgrade result:', res.stdout, res.stderr);

    ssh.dispose();
  } catch (err) {
    console.error('Error:', err);
  }
}

main();
