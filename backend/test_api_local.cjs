const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
const http = require('http');
const jwt = require('jsonwebtoken');

async function main() {
  const admin = await prisma.user.findUnique({ where: { phone: '13828577665' } });
  if (!admin) return console.log("Admin not found");
  
  const token = jwt.sign({ sub: admin.id, role: 'ADMIN' }, process.env.JWT_SECRET || 'lslife-super-secret-key-2024-jwt', { expiresIn: '1h' });
  
  http.get({
    hostname: '127.0.0.1',
    port: 3000,
    path: '/api/admin/dashboard',
    headers: { 'Authorization': 'Bearer ' + token }
  }, res => {
    let body = '';
    res.on('data', d => body += d);
    res.on('end', () => {
      console.log("HTTP Code:", res.statusCode);
      console.log("Response:", body);
    });
  });
}
main().finally(() => prisma.$disconnect());
