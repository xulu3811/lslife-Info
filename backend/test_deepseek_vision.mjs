import fs from 'fs';

async function testDeepseekVision() {
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
        {
          role: 'user',
          content: [
            { type: 'text', text: 'What is this image about?' },
            { type: 'image_url', image_url: { url: 'https://mentalhlp.site/uploads/test.jpg' } }
          ]
        }
      ]
    })
  });
  
  const text = await response.text();
  console.log("Deepseek Vision Response:", text);
}

testDeepseekVision();
