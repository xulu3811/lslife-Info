import pkg from 'pg';
const { Client } = pkg;

async function main() {
  console.log('Connecting to production PostgreSQL database via pg...');

  // 使用 pg 直接连接到生产库，避开 Prisma 的 sqlite 驱动校验
  const client = new Client({
    connectionString: 'postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@115.191.6.95:5433/lslife?schema=public'
  });

  await client.connect();
  console.log('✅ Connected successfully.\n');

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
    try {
      const res = await client.query(
        'UPDATE "Category" SET "iconUrl" = $1 WHERE "name" = $2 RETURNING id;',
        [item.iconUrl, item.name]
      );
      
      if (res.rowCount > 0) {
        console.log(`✅ Successfully restored icon for [${item.name}] -> ${item.iconUrl}`);
      } else {
        console.log(`⚠️ Category not found in DB: ${item.name}`);
      }
    } catch (error) {
      console.error(`❌ Error updating ${item.name}:`, error.message);
    }
  }

  await client.end();
  console.log('\nDatabase disconnected. Restore operation completed!');
}

main().catch(console.error);
