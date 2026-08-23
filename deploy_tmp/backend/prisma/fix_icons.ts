import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

const iconMapping: Record<string, string> = {
  '整租/合租': '/assets/icons/3d_flat_house_rent.png',
  '日租/民宿': '/assets/icons/3d_flat_house_short.png',
  '家庭保洁': '/assets/icons/3d_flat_service_daily.png',
  '开荒保洁': '/assets/icons/3d_flat_service_new.png',
  '家电清洗': '/assets/icons/3d_flat_service_appliance.png',
  '保姆/钟点': '/assets/icons/3d_flat_service_nanny.png',
  '育儿/陪护': '/assets/icons/3d_flat_service_maternity.png',
  '搬家/货运': '/assets/icons/3d_flat_service_moving.png',
};

async function main() {
  for (const [name, iconUrl] of Object.entries(iconMapping)) {
    const result = await prisma.category.updateMany({
      where: { name: name },
      data: { iconUrl: iconUrl }
    });
    console.log(`Updated ${name}: ${result.count} rows`);
  }
}

main()
  .catch(console.error)
  .finally(async () => {
    await prisma.$disconnect();
  });
