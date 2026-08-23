import fetch from 'node-fetch';

const BASE_URL = 'https://mentalhlp.site/api';
// Using test1 user
const loginBody = { phone: 'test1', password: 'ls441825', role: 'USER' };

async function runTests() {
  console.log('--- Commercial Integration Test Suite ---');
  
  // 1. Login
  console.log('\n[1] Logging in...');
  const loginRes = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(loginBody)
  });
  const loginData = await loginRes.json();
  if (!loginRes.ok) {
    console.error('Login failed:', loginData);
    return;
  }
  const token = loginData.data.token;
  console.log('v" Login successful');

  // 2. Merchant Apply
  console.log('\n[2] Testing Merchant Apply API (/api/merchants/apply)...');
  const applyRes = await fetch(`${BASE_URL}/merchants/apply`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({
      shopName: 'Test Commercial Shop',
      contactPhone: '13800138000',
      address: 'Test Address 123'
    })
  });
  const applyData = await applyRes.json();
  if (applyRes.ok) {
    console.log('v" Merchant Apply passed:', applyData.message);
  } else {
    console.error('x" Merchant Apply failed:', applyData);
  }

  // 3. Merchant Certify
  console.log('\n[3] Testing Merchant Certify API (/api/merchants/certify)...');
  const certifyRes = await fetch(`${BASE_URL}/merchants/certify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({
      certType: 'ENTERPRISE',
      storeName: 'Test Certify Store',
      contactName: 'Mr. Test',
      contactPhone: '13800138000'
    })
  });
  const certifyData = await certifyRes.json();
  if (certifyRes.ok) {
    console.log('v" Merchant Certify passed:', certifyData.message);
  } else {
    console.error('x" Merchant Certify failed:', certifyData);
  }

  // 4. Certify Status
  console.log('\n[4] Testing Certify Status API (/api/merchants/certify/status)...');
  const statusRes = await fetch(`${BASE_URL}/merchants/certify/status`, {
    method: 'GET',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const statusData = await statusRes.json();
  if (statusRes.ok) {
    console.log('v" Certify Status passed:', statusData.data ? 'Status: ' + statusData.data.status : 'No cert');
  } else {
    console.error('x" Certify Status failed:', statusData);
  }

  console.log('\n--- Test Suite Complete ---');
}

runTests().catch(console.error);
