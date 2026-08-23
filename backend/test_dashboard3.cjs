const http = require('https');

const data = JSON.stringify({ phone: '13828577665', password: '123' });

const req = http.request({
  hostname: 'mentalhlp.site',
  port: 443,
  path: '/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
}, res => {
  let body = '';
  res.on('data', d => body += d);
  res.on('end', () => {
    const json = JSON.parse(body);
    const token = json.data.token;
    console.log("Token received.");
    
    http.get({
      hostname: 'mentalhlp.site',
      port: 443,
      path: '/api/admin/dashboard',
      headers: { 'Authorization': 'Bearer ' + token }
    }, res2 => {
      let body2 = '';
      res2.on('data', d => body2 += d);
      res2.on('end', () => {
        console.log("Dashboard HTTP Code:", res2.statusCode);
        console.log("Dashboard response:", body2);
      });
    });
  });
});
req.write(data);
req.end();
