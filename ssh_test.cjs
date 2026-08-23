const { execSync } = require('child_process');

console.log("Starting SSH test...");
try {
  const out = execSync("ssh -o StrictHostKeyChecking=no lslife@115.191.6.95 ls -la", { encoding: 'utf-8', stdio: 'pipe' });
  console.log("Output:");
  console.log(out);
} catch (e) {
  console.error("Error:");
  console.error(e.message);
  console.error(e.stderr);
}
