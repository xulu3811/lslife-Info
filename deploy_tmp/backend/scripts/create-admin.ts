import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';
const prisma = new PrismaClient();

async function main() {
  let admin = await prisma.adminUser.findFirst();
  if(!admin) {
    const hash = await bcrypt.hash("123456", 10);
    admin = await prisma.adminUser.create({
      data: { username: "admin", password: hash, role: "superadmin" }
    });
    console.log("Created admin: admin / 123456");
  } else {
    console.log("Admin exists. Username is:", admin.username);
  }
}
main().catch(console.error).finally(() => prisma.$disconnect());
