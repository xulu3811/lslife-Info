import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const updates = [
    { name: '个人闲置', iconUrl: '/assets/icons/3d_flat_secondhand.png' },
    { name: '房屋租售', iconUrl: '/assets/icons/3d_flat_housing.png' },
    { name: '家政保洁', iconUrl: '/assets/icons/3d_flat_cleaning.png' },
    { name: '水电维修', iconUrl: '/assets/icons/3d_flat_repair.png' },
    { name: '水果蔬菜', iconUrl: '/assets/icons/3d_flat_produce.png' },
    { name: '招聘求职', iconUrl: '/assets/icons/3d_flat_jobs.png' },
    { name: '租车服务', iconUrl: '/assets/icons/3d_flat_car_rental.png' },
    { name: '兼职零工', iconUrl: '/assets/icons/3d_flat_parttime.png' }
  ];

  console.log('Starting home category updates...');
  for (const item of updates) {
    const res = await prisma.category.updateMany({
      where: { name: item.name },
      data: { iconUrl: item.iconUrl }
    });
    console.log(`Updated ${item.name}: ${res.count} records`);
  }
  console.log('Update complete');
}

main()
  .catch(e => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
