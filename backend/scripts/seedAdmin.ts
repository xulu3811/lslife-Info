import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

/**
 * 创建/重置管理员账号。
 * 用法: npx tsx scripts/seedAdmin.ts <username> <password>
 * 或: ADMIN_USERNAME=... ADMIN_PASSWORD=... npx tsx scripts/seedAdmin.ts
 * 禁止在仓库中硬编码生产密码。
 */
const prisma = new PrismaClient();

async function main() {
  const username = process.argv[2] || process.env.ADMIN_USERNAME;
  const password = process.argv[3] || process.env.ADMIN_PASSWORD;

  if (!username || !password) {
    console.error('用法: npx tsx scripts/seedAdmin.ts <username> <password>');
    process.exit(1);
  }
  if (password.length < 10) {
    console.error('密码长度至少 10 位');
    process.exit(1);
  }

  const hashedPassword = await bcrypt.hash(password, 10);

  const admin = await prisma.adminUser.upsert({
    where: { username },
    update: { password: hashedPassword, role: 'superadmin' },
    create: {
      username,
      password: hashedPassword,
      role: 'superadmin',
    },
  });

  console.log(`\n管理员账户配置成功`);
  console.log(`  用户名: ${admin.username}`);
  console.log(`  角色:   ${admin.role}\n`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
