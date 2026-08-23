import { NodeSSH } from 'node-ssh';
const ssh = new NodeSSH();
async function main() {
  await ssh.connect({ host: '115.191.6.95', username: 'root', password: 'Maxence2468;' });

  // Create a test script on the server
  const testScript = `
import Tesseract from 'tesseract.js';
async function test() {
  console.log("Starting OCR test...");
  try {
    const { data: { text } } = await Tesseract.recognize(
      'https://tesseract.projectnaptha.com/img/eng_bw.png',
      'eng',
      { logger: m => console.log(m.status, m.progress) }
    );
    console.log("Result:", text);
  } catch (e) {
    console.error(e);
  }
}
test();
`;
  
  await ssh.execCommand(`cat << 'EOF' > /home/lslife/lslife-backend/test_tesseract.mjs\n${testScript}\nEOF`);
  const res = await ssh.execCommand("su - lslife -c 'cd /home/lslife/lslife-backend && node test_tesseract.mjs'");
  console.log(res.stdout);
  console.log(res.stderr);
  ssh.dispose();
}
main().catch(console.error);
