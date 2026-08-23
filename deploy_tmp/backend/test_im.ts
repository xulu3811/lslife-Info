import axios from 'axios';
import WebSocket from 'ws';

const API_URL = 'https://mentalhlp.site/api';
const WS_URL = 'wss://mentalhlp.site/ws';

async function delay(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function runTest() {
  try {
    console.log("Registering test users...");
    
    // TestUserA
    let tokenA, tokenB, idA, idB;
    try {
        const regA = await axios.post(`${API_URL}/auth/register`, { phone: '13800138001', password: 'Test1234a', nickname: 'TestUserA' });
        tokenA = regA.data.data.token;
        idA = regA.data.data.user.id;
    } catch (e: any) {
        // If already registered, login
        const loginA = await axios.post(`${API_URL}/auth/login`, { phone: '13800138001', password: 'Test1234a' });
        tokenA = loginA.data.data.token;
        idA = loginA.data.data.user.id;
    }

    // TestUserB
    try {
        const regB = await axios.post(`${API_URL}/auth/register`, { phone: '13800138002', password: 'Test1234a', nickname: 'TestUserB' });
        tokenB = regB.data.data.token;
        idB = regB.data.data.user.id;
    } catch (e: any) {
        const loginB = await axios.post(`${API_URL}/auth/login`, { phone: '13800138002', password: 'Test1234a' });
        tokenB = loginB.data.data.token;
        idB = loginB.data.data.user.id;
    }

    console.log(`User A (id: ${idA}) token: ${tokenA.substring(0,10)}...`);
    console.log(`User B (id: ${idB}) token: ${tokenB.substring(0,10)}...`);

    console.log("Connecting WS...");
    const wsA = new WebSocket(`${WS_URL}?token=${tokenA}`);
    const wsB = new WebSocket(`${WS_URL}?token=${tokenB}`);

    let wsAReady = false;
    let wsBReady = false;

    wsA.on('open', () => { wsAReady = true; console.log("User A WS open"); });
    wsB.on('open', () => { wsBReady = true; console.log("User B WS open"); });

    let firstMsgId: string | null = null;
    wsA.on('message', (data) => {
        const str = data.toString();
        console.log("User A received:", str);
        const obj = JSON.parse(str);
        if (obj.event === 'chat_message' && obj.message && obj.message.content === 'Stress test message 0') {
            firstMsgId = obj.message.id;
        }
    });
    wsB.on('message', (data) => console.log("User B received:", data.toString()));

    wsA.on('error', (err) => console.error("User A WS error", err));
    wsB.on('error', (err) => console.error("User B WS error", err));

    while (!wsAReady || !wsBReady) {
      await delay(100);
    }

    console.log("Sending message 0...");
    const msgId = `test_msg_${Date.now()}`;
    const payload = {
        action: 'chat',
        clientMsgId: msgId,
        toUserId: idB,
        targetId: null,
        content: `Stress test message 0`,
        type: 'TEXT'
    };
    wsA.send(JSON.stringify(payload));
    await delay(2000);

    if (firstMsgId) {
        console.log(`Recalling message ${firstMsgId}...`);
        wsA.send(JSON.stringify({
            action: 'recall',
            messageId: firstMsgId
        }));
    }

    await delay(3000);
    console.log("Stress test finished, closing...");
    wsA.close();
    wsB.close();
  } catch (e: any) {
    console.error("Test failed:", e.response?.data || e.message);
  }
}

runTest();
