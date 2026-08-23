import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

const sqlScript = `
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_digital.png' WHERE "name" IN ('数码 3C', '数码3C');
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_clothing.png' WHERE "name" = '服饰箱包';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_appliance.png' WHERE "name" = '日用/家电';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_beauty.png' WHERE "name" = '美妆个护';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_maternal.png' WHERE "name" = '母婴儿童';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_sports.png' WHERE "name" = '运动 & 交通工具';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_entertainment.png' WHERE "name" = '文娱爱好';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_others.png' WHERE "name" = '其它';

UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_produce.png' WHERE "name" = '新鲜水果';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_veg_fresh.png' WHERE "name" = '时令蔬菜';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_veg_meat.png' WHERE "name" = '肉禽蛋品';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_veg_local.png' WHERE "name" = '海鲜水产';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_veg_wholesale.png' WHERE "name" = '冷藏冻货';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_veg_grocery.png' WHERE "name" = '粮油调味';
UPDATE "Category" SET "iconUrl" = 'https://mentalhlp.site/assets/icons/3d_flat_dining.png' WHERE "name" = '熟食卤味';
`;

async function main() {
  console.log('Connecting to server via SSH...');
  await ssh.connect({
    host: '115.191.6.95',
    username: 'root',
    password: 'Maxence2468;'
  });

  console.log('✅ SSH connected. Executing SQL update...');

  // 通过环境变量传递密码给 psql，并直接执行 SQL 字符串
  const result = await ssh.execCommand(`PGPASSWORD='af4a98b163543c58c46bf827bdd546a8' psql -h 127.0.0.1 -p 5433 -U lslife -d lslife -c "${sqlScript.replace(/"/g, '\\"')}"`);

  if (result.stderr && !result.stderr.includes('UPDATE')) {
      console.log('⚠️ output/error:', result.stderr);
  }
  if (result.stdout) {
      console.log('✅ Result:\n', result.stdout);
  }

  ssh.dispose();
  console.log('All icons restored successfully!');
}

main().catch(console.error);
