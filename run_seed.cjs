const { execSync } = require('child_process');
const fs = require('fs');

const b64 = fs.readFileSync('backend/scripts/seed_test_accounts.ts').toString('base64');
const cmd = `ssh lslife@115.191.6.95 "echo ${b64} | base64 -d > /home/lslife/lslife-backend/scripts/seed_test_accounts.ts && cd /home/lslife/lslife-backend && source ~/.bashrc && source ~/.nvm/nvm.sh && npx tsx scripts/seed_test_accounts.ts"`;

try {
    const out = execSync(cmd, { encoding: 'utf-8', stdio: 'inherit' });
    console.log(out);
} catch (e) {
    console.error(e);
}
