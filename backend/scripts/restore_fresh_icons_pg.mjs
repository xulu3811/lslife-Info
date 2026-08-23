import pkg from 'pg';
const { Client } = pkg;

async function main() {
  console.log('Connecting to production PostgreSQL database via pg...');

  // 使用 pg 直接连接到生产库
  const client = new Client({
    connectionString: 'postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@115.191.6.95:5433/lslife?schema=public'
  });

  await client.connect();
  console.log('✅ Connected successfully.\n');

  // 生鲜类目图标映射表
  const iconMappings = [
    { name: '新鲜水果', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_produce.png' },
    { name: '时令蔬菜', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_veg_fresh.png' },
    { name: '肉禽蛋品', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_veg_meat.png' },
    { name: '海鲜水产', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_veg_local.png' },
    { name: '冷藏冻货', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_veg_wholesale.png' },
    { name: '粮油调味', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_veg_grocery.png' },
    { name: '熟食卤味', iconUrl: 'https://mentalhlp.site/assets/icons/3d_flat_dining.png' }
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
  console.log('\nDatabase disconnected. Fresh category icons restore operation completed!');
}

main().catch(console.error);
