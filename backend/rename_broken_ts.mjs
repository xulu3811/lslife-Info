import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();
const host = '115.191.6.95';
const username = 'root';
const password = 'Maxence2468;';

async function main() {
  await ssh.connect({ host, username, password });
  const result = await ssh.execCommand(
    'cd /home/lslife/lslife-backend/src/modules && mv payments.ts payments.bak && mv orders.ts orders.bak && mv cart.ts cart.bak && cd ../services && mv ai.ts ai.bak && mv delivery.ts delivery.bak && mv order-fulfillment.ts order-fulfillment.bak'
  );
  console.log(result.stdout || result.stderr);
}
main().then(() => ssh.dispose());
