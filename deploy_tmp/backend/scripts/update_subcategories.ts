import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

const updates = [
  { id: 'cat_house_rent', icon: '3d_flat_house_rent.png' },
  { id: 'cat_house_sale', icon: '3d_flat_house_sale.png' },
  { id: 'cat_house_shop', icon: '3d_flat_house_shop.png' },
  { id: 'cat_house_factory', icon: '3d_flat_house_factory.png' },
  { id: 'cat_house_short', icon: '3d_flat_house_short.png' },
  { id: 'cat_main_lock', icon: '3d_flat_main_lock.png' },
  { id: 'cat_main_pipe', icon: '3d_flat_main_pipe.png' },
  { id: 'cat_main_electrical', icon: '3d_flat_main_electrical.png' },
  { id: 'cat_main_appliance', icon: '3d_flat_main_appliance.png' },
  { id: 'cat_main_waterproof', icon: '3d_flat_main_waterproof.png' },
  { id: 'cat_service_daily', icon: '3d_flat_service_daily.png' },
  { id: 'cat_service_new', icon: '3d_flat_service_new.png' },
  { id: 'cat_service_appliance', icon: '3d_flat_service_appliance.png' },
  { id: 'cat_service_nanny', icon: '3d_flat_service_nanny.png' },
  { id: 'cat_service_maternity', icon: '3d_flat_service_maternity.png' },
  { id: 'cat_service_moving', icon: '3d_flat_service_moving.png' },
  { id: 'cat_veg_fresh', icon: '3d_flat_veg_fresh.png' },
  { id: 'cat_veg_meat', icon: '3d_flat_veg_meat.png' },
  { id: 'cat_veg_grocery', icon: '3d_flat_veg_grocery.png' },
  { id: 'cat_veg_local', icon: '3d_flat_veg_local.png' },
  { id: 'cat_veg_wholesale', icon: '3d_flat_veg_wholesale.png' },
  { id: 'cat_car_suv', icon: '3d_flat_car_suv.png' },
  { id: 'cat_car_luxury', icon: '3d_flat_car_luxury.png' },
  { id: 'cat_car_bus', icon: '3d_flat_car_bus.png' },
  { id: 'cat_car_truck', icon: '3d_flat_car_truck.png' },
  { id: 'cat_car_ebike', icon: '3d_flat_car_ebike.png' },
  { id: 'cat_job_hospitality', icon: '3d_flat_job_hospitality.png' },
  { id: 'cat_job_blue_collar', icon: '3d_flat_job_blue_collar.png' },
  { id: 'cat_job_sales', icon: '3d_flat_job_sales.png' },
  { id: 'cat_job_admin', icon: '3d_flat_job_admin.png' },
  { id: 'cat_job_logistics', icon: '3d_flat_job_logistics.png' },
  { id: 'cat_job_other', icon: '3d_flat_job_other.png' },
  { id: 'cat_pt_temp', icon: '3d_flat_pt_temp.png' },
  { id: 'cat_pt_promo', icon: '3d_flat_pt_promo.png' },
  { id: 'cat_pt_hotel', icon: '3d_flat_pt_hotel.png' },
  { id: 'cat_pt_tutor', icon: '3d_flat_pt_tutor.png' },
  { id: 'cat_pt_errand', icon: '3d_flat_pt_errand.png' }
];

async function main() {
  for (const update of updates) {
    try {
      await prisma.category.update({
        where: { id: update.id },
        data: { iconUrl: `/assets/icons/${update.icon}` }
      });
      console.log(`Updated ${update.id}`);
    } catch (e) {
      console.log(`Failed to update ${update.id}: ${e}`);
    }
  }
}

main().finally(() => prisma.$disconnect());
