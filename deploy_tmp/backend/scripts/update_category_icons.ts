import { PrismaClient } from '@prisma/client';

// 连接到生产环境的 PostgreSQL
const prisma = new PrismaClient({
  datasources: {
    db: {
      url: 'postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@115.191.6.95:5433/lslife?schema=public'
    }
  }
});

async function main() {
  console.log('Connecting to production database...');
  
  // 更新分类图标
  const updates = [
    { name: '个人闲置', iconUrl: 'https://mentalhlp.site/assets/icons/idle.webp' },
    { name: '房屋租售', iconUrl: 'https://mentalhlp.site/assets/icons/house.webp' },
    { name: '家政保洁', iconUrl: 'https://mentalhlp.site/assets/icons/cleaning.webp' },
    { name: '水电维修', iconUrl: 'https://mentalhlp.site/assets/icons/repair.webp' },
    // 兼容可能存在的不同名称
    { name: '拼车/租车', iconUrl: 'https://mentalhlp.site/assets/icons/carpool.webp' },
    { name: '拼车租车', iconUrl: 'https://mentalhlp.site/assets/icons/carpool.webp' }
  ];

  for (const item of updates) {
    const category = await prisma.category.findFirst({
      where: { name: item.name }
    });

    if (category) {
      await prisma.category.update({
        where: { id: category.id },
        data: { iconUrl: item.iconUrl }
      });
      console.log(`✅ Updated ${item.name} iconUrl to ${item.iconUrl}`);
    } else {
      console.log(`⚠️ Category not found in DB: ${item.name}`);
    }
  }
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
    console.log('Database disconnected. All done!');
  });
