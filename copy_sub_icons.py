import os
import shutil

ASSETS_DIR = r"d:\LsLife\backend\public\assets\icons"
DRAWABLE_DIR = r"d:\LsLife\android\app\src\main\res\drawable"

mapping = {
    # 1. 个人闲置
    "ic_category_sub_home_appliances.png": "3d_flat_appliance.png",
    "ic_category_sub_sports_mobility.png": "3d_flat_sports.png",
    "ic_category_sub_entertainment.png": "3d_flat_entertainment.png",
    "ic_category_sub_other_idle.png": "3d_flat_others.png",

    # 2. 家政/护理
    "ic_category_sub_daily_cleaning.png": "3d_flat_service_daily.png",
    "ic_category_sub_deep_cleaning.png": "3d_flat_cleaning.png",
    "ic_category_sub_nanny_hourly.png": "3d_flat_service_nanny.png",
    "ic_category_sub_maternity_childcare.png": "3d_flat_service_maternity.png",
    "ic_category_sub_caregiving.png": "3d_flat_service_new.png",

    # 3. 便民维修 & 同城生鲜
    "ic_category_sub_digital_repair.png": "3d_flat_digital.png",
    "ic_category_sub_frozen_food.png": "3d_flat_veg_wholesale.png",

    # 4. 房屋出租 & 二手房产
    "ic_category_sub_warehouse_rent.png": "3d_flat_house_factory.png",
    "ic_category_sub_short_term_rent.png": "3d_flat_house_short.png",
    "ic_category_sub_parking_rent.png": "3d_flat_housing.png",
    "ic_category_sub_house_sale.png": "3d_flat_house_sale.png",
    "ic_category_sub_land_factory_transfer.png": "3d_flat_house_factory.png",
    "ic_category_sub_parking_sale.png": "3d_flat_housing.png",
    "ic_category_sub_new_property.png": "3d_flat_house_shop.png",

    # 5. 拼车/租车
    "ic_category_sub_car_rental.png": "3d_flat_car_rental.png",
    "ic_category_sub_wedding_car.png": "3d_flat_car_luxury.png",
    "ic_category_sub_bus_construction_rent.png": "3d_flat_car_bus.png",

    # 6. 招聘求职
    "ic_category_sub_full_time_job.png": "3d_flat_jobs.png",
    "ic_category_sub_part_time_daily.png": "3d_flat_parttime.png",
    "ic_category_sub_catering_service_job.png": "3d_flat_job_hospitality.png",
    "ic_category_sub_factory_worker_job.png": "3d_flat_job_blue_collar.png",
    "ic_category_sub_admin_finance_job.png": "3d_flat_job_admin.png",

    # 7. 本地生活 & 教育培训
    "ic_category_sub_gourmet_dining.png": "3d_flat_dining.png",
    "ic_category_sub_recreation_entertainment.png": "3d_flat_entertainment.png",
    "ic_category_sub_wedding_photography.png": "3d_flat_beauty.png",
    "ic_category_sub_agritourism_travel.png": "3d_flat_veg_local.png",
    "ic_category_sub_beauty_spa.png": "3d_flat_beauty.png",
    "ic_category_sub_academic_tutoring.png": "3d_flat_education.png",
    "ic_category_sub_arts_sports.png": "3d_flat_sports.png",
    "ic_category_sub_vocational_certification.png": "3d_flat_pt_tutor.png",
    "ic_category_sub_driving_school.png": "3d_flat_car_suv.png",
    "ic_category_sub_ai_coding.png": "3d_flat_digital.png",
    "ic_category_sub_early_education.png": "3d_flat_maternal.png"
}

def main():
    print(f"Copying {len(mapping)} subcategory icons...")
    for target_name, src_name in mapping.items():
        src_path = os.path.join(ASSETS_DIR, src_name)
        dst_path = os.path.join(DRAWABLE_DIR, target_name)
        shutil.copyfile(src_path, dst_path)
        print(f"Copied {src_name} -> {target_name}")

if __name__ == "__main__":
    main()
