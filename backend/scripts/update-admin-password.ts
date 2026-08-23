import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';
const prisma = new PrismaClient();

async function main() {
  const username = "root";
  const newPassword = process.argv[2] || "LsLife@Admin2026!";
  const hash = await bcrypt.hash(newPassword, 10);

  const admin = await prisma.adminUser.findUnique({
    where: { username }
  });

  if (!admin) {
    console.log(`Admin user '${username}' not found!`);
  } else {
    await prisma.adminUser.update({
      where: { username },
      data: { password: hash }
    });
    console.log(`Password for '${username}' has been updated successfully.`);
    console.log(`New password is: ${newPassword}`);
  }
}

main().catch(console.error).finally(() => prisma.$disconnect());
