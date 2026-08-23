const fs = require('fs');
const path = require('path');

const seedPath = path.join(__dirname, '../prisma/seed.ts');
let content = fs.readFileSync(seedPath, 'utf-8');

const iconMapping = {
    // 个人闲置
    'second_hand': '3d_flat_digital.png',
    'cat_3c_pc': '3d_flat_digital.png',
    'cat_3c_camera': '3d_flat_digital.png',
    'cat_3c_audio': '3d_flat_digital.png',
    'cat_dress': '3d_flat_clothing.png',
    'cat_bag': '3d_flat_clothing.png',
    'cat_luxury': '3d_flat_clothing.png',
    'cat_home_daily': '3d_flat_appliance.png',
    'cat_home_appliance': '3d_flat_appliance.png',
    'cat_home_furniture': '3d_flat_appliance.png',
    'cat_beauty_skincare': '3d_flat_beauty.png',
    'cat_beauty_makeup': '3d_flat_beauty.png',
    'cat_beauty_perfume': '3d_flat_beauty.png',
    'cat_baby_stroller': '3d_flat_maternal.png',
    'cat_baby_toy': '3d_flat_maternal.png',
    'cat_baby_clothing': '3d_flat_maternal.png',
    'cat_sports_equipment': '3d_flat_sports.png',
    'cat_sports_ebike': '3d_flat_car_ebike.png',
    'cat_hobby_book': '3d_flat_entertainment.png',
    'cat_hobby_instrument': '3d_flat_entertainment.png',
    'cat_hobby_game': '3d_flat_entertainment.png',
    'cat_other_ticket': '3d_flat_others.png',
    'cat_other_pet': '3d_flat_others.png',
    'cat_other_plant': '3d_flat_others.png',

    // 房屋租售
    'cat_house_rent': '3d_flat_house_rent.png',
    'cat_house_sale': '3d_flat_house_sale.png',
    'cat_house_shop': '3d_flat_house_shop.png',
    'cat_house_factory': '3d_flat_house_factory.png',
    'cat_house_short': '3d_flat_house_short.png',

    // 家政保洁
    'cat_service_daily': '3d_flat_service_daily.png',
    'cat_service_new': '3d_flat_service_new.png',
    'cat_service_appliance': '3d_flat_service_appliance.png',
    'cat_service_nanny': '3d_flat_service_nanny.png',
    'cat_service_maternity': '3d_flat_service_maternity.png',
    'cat_service_moving': '3d_flat_service_moving.png',

    // 水电维修
    'cat_main_lock': '3d_flat_main_lock.png',
    'cat_main_pipe': '3d_flat_main_pipe.png',
    'cat_main_electrical': '3d_flat_main_electrical.png',
    'cat_main_appliance': '3d_flat_main_appliance.png',
    'cat_main_waterproof': '3d_flat_main_waterproof.png',

    // 招聘求职
    'job_hospitality': '3d_flat_job_hospitality.png',
    'job_blue_collar': '3d_flat_job_blue_collar.png',
    'job_sales': '3d_flat_job_sales.png',
    'job_admin': '3d_flat_job_admin.png',
    'job_logistics': '3d_flat_job_logistics.png',
    'job_other_all': '3d_flat_job_other.png',

    // 兼职零工
    'pt_temp': '3d_flat_pt_temp.png',
    'pt_promo': '3d_flat_pt_promo.png',
    'pt_hotel': '3d_flat_pt_hotel.png',
    'pt_tutor': '3d_flat_pt_tutor.png',
    'pt_errand': '3d_flat_pt_errand.png',

    // 拼车/租车
    'car_carpool_person_find_car': '3d_flat_car_carpool_person.png',
    'car_carpool_car_find_person': '3d_flat_car_suv.png',
    'car_carpool_goods': '3d_flat_pt_errand.png',
    'car_rental_suv': '3d_flat_car_suv.png',
    'car_rental_wedding': '3d_flat_car_luxury.png',
    'car_rental_bus': '3d_flat_car_bus.png',
    'car_rental_truck': '3d_flat_car_truck.png',

    // 同城生鲜
    'cat_veg_fresh': '3d_flat_veg_fresh.png',
    'cat_veg_meat': '3d_flat_veg_meat.png',
    'cat_veg_grocery': '3d_flat_veg_grocery.png',
    'cat_veg_local': '3d_flat_veg_local.png',
    'cat_veg_wholesale': '3d_flat_veg_wholesale.png',

    // 教育培训
    'cat_edu_k12': '3d_flat_education.png',
    'cat_edu_art': '3d_flat_education.png',
    'cat_edu_driving': '3d_flat_education.png',
    'cat_edu_vocational': '3d_flat_education.png',
    'cat_edu_english': '3d_flat_education.png',
    'cat_edu_ai': '3d_flat_education.png',

    // 餐饮娱乐
    'cat_dine_food': '3d_flat_dining.png',
    'cat_dine_new': '3d_flat_dining.png',
    'cat_dine_ktv': '3d_flat_entertainment.png',
};

for (const [id, icon] of Object.entries(iconMapping)) {
    const idRegex = new RegExp("(id:\\s*'" + id + "',\\s*\\n\\s*name:\\s*'.*?',\\s*\\n\\s*icon:\\s*'.*?',)", 'g');
    
    if (!content.includes("iconUrl: '/assets/icons/" + icon + "',")) {
        content = content.replace(idRegex, (match, p1) => {
            return p1 + "\n            iconUrl: '/assets/icons/" + icon + "',";
        });
    }
}

fs.writeFileSync(seedPath, content, 'utf-8');
console.log('Successfully injected iconUrls into seed.ts');
