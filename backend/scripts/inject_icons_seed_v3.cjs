const fs = require('fs');
const path = require('path');

const seedPath = path.join(__dirname, '../prisma/seed.ts');
let content = fs.readFileSync(seedPath, 'utf-8');

const prefixMapping = {
    'cat_3c_': '3d_flat_digital.png',
    'cat_beauty_': '3d_flat_beauty.png',
    'cat_baby_': '3d_flat_maternal.png',
    'cat_sports_': '3d_flat_sports.png',
    'cat_hobby_': '3d_flat_entertainment.png',
    'cat_other_': '3d_flat_others.png',
    'house_rent_': '3d_flat_house_rent.png',
    'house_sale_': '3d_flat_house_sale.png',
    'house_shop_': '3d_flat_house_shop.png',
    'house_fac_': '3d_flat_house_factory.png',
    'house_short_': '3d_flat_house_short.png',
    'service_daily_': '3d_flat_service_daily.png',
    'service_new_': '3d_flat_service_new.png',
    'service_appliance_': '3d_flat_service_appliance.png',
    'service_nanny_': '3d_flat_service_nanny.png',
    'service_mat_': '3d_flat_service_maternity.png',
    'service_moving_': '3d_flat_service_moving.png',
    'main_lock_': '3d_flat_main_lock.png',
    'main_pipe_': '3d_flat_main_pipe.png',
    'main_electrical_': '3d_flat_main_electrical.png',
    'main_appliance_': '3d_flat_main_appliance.png',
    'main_waterproof_': '3d_flat_main_waterproof.png',
    'job_hosp_': '3d_flat_job_hospitality.png',
    'job_blue_': '3d_flat_job_blue_collar.png',
    'job_sales_': '3d_flat_job_sales.png',
    'job_admin_': '3d_flat_job_admin.png',
    'job_logistics_': '3d_flat_job_logistics.png',
    'job_other_': '3d_flat_job_other.png',
    'pt_temp_': '3d_flat_pt_temp.png',
    'pt_promo_': '3d_flat_pt_promo.png',
    'pt_hotel_': '3d_flat_pt_hotel.png',
    'pt_tutor_': '3d_flat_pt_tutor.png',
    'pt_errand_': '3d_flat_pt_errand.png',
    'edu_': '3d_flat_education.png',
    'dining_': '3d_flat_dining.png',
    'cat_veg_': '3d_flat_fresh_food.png',
};

// First, get all matches
const regex = /id:\s*'([^']+)',\s*\n\s*name:\s*'([^']+)',\s*\n\s*icon:\s*'([^']+)',(?!\s*\n\s*iconUrl:)/g;
let match;
let matchCount = 0;

while ((match = regex.exec(content)) !== null) {
    const id = match[1];
    let iconToInject = null;

    // Check against prefix mapping
    for (const [prefix, icon] of Object.entries(prefixMapping)) {
        if (id.startsWith(prefix)) {
            iconToInject = icon;
            break;
        }
    }

    if (iconToInject) {
        const searchString = match[0];
        const replacement = searchString + "\n            iconUrl: '/assets/icons/" + iconToInject + "',";
        content = content.replace(searchString, replacement);
        matchCount++;
        // Reset regex index because we modified the string
        regex.lastIndex = 0;
    }
}

fs.writeFileSync(seedPath, content, 'utf-8');
console.log('Successfully injected', matchCount, 'iconUrls into seed.ts');
