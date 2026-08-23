import fs from 'fs';

async function testDashscope() {
  const key = "sk-30f79d21acbd487da71ec3cb5ce63d54";
  
  // Test Dashscope Qwen-VL
  const response = await fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${key}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      model: 'qwen-vl-plus',
      messages: [
        {
          role: 'user',
          content: [
            { type: 'text', text: 'Please extract the name and ID number from this Chinese ID card image. Return JSON: {"name": "...", "id": "..."}' },
            { type: 'image_url', image_url: { url: 'https://mentalhlp.site/uploads/test.jpg' } } // Assuming the server can access this or I just use a placeholder text to see if the key works
          ]
        }
      ]
    })
  });
  
  const text = await response.text();
  console.log("Dashscope Response:", text);
}

async function testDeepseek() {
  const key = "sk-30f79d21acbd487da71ec3cb5ce63d54";
  
  const response = await fetch('https://api.deepseek.com/chat/completions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${key}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      model: 'deepseek-chat',
      messages: [
        { role: 'user', content: 'Hello' }
      ]
    })
  });
  
  const text = await response.text();
  console.log("Deepseek Response:", text);
}

async function main() {
  await testDeepseek();
  await testDashscope();
}

main();
