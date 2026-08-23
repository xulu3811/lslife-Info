const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');

const prisma = new PrismaClient();

async function main() {
  const username = 'root';
  const password = 'NtktiC726Kbmt3oMM8B5EgVQ';
  
  const existingAdmin = await prisma.adminUser.findUnique({ where: { username } });
  if (existingAdmin) {
    console.log('Admin user already exists.');
    return;
  }

  const hashedPassword = await bcrypt.hash(password, 10);
  
  await prisma.adminUser.create({
    data: {
      username,
      password: hashedPassword,
      role: 'superadmin'
    }
  });
  
  console.log('Admin user created successfully.');
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
