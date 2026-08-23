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
  console.log('Connecting to production database to restore secondary category icons...');
  
  // 图标映射表
  const iconMappings = [
    { name: '数码 3C', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_digital.png' },
    { name: '服饰箱包', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_clothing.png' },
    { name: '日用/家电', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_appliance.png' },
    { name: '美妆个护', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_beauty.png' },
    { name: '母婴儿童', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_maternal.png' },
    { name: '运动 & 交通工具', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_sports.png' },
    { name: '文娱爱好', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_entertainment.png' },
    { name: '其它', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_others.png' }
  ];

  for (const item of iconMappings) {
    const categories = await prisma.category.findMany({
      where: { name: item.name }
    });

    if (categories.length > 0) {
      for (const category of categories) {
        await prisma.category.update({
          where: { id: category.id },
          data: { iconUrl: item.iconUrl }
        });
        console.log(`✅ Successfully restored icon for [${item.name}] -> ${item.iconUrl}`);
      }
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
    console.log('Database disconnected. Restore operation completed!');
  });
