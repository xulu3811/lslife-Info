import os
import shutil

ASSETS_DIR = r"d:\LsLife\backend\public\assets\icons"
DRAWABLE_DIR = r"d:\LsLife\android\app\src\main\res\drawable"

copies = [
    # (src, dst1, dst2)
    ("3d_flat_service_daily.png", "ic_category_sub_daily_cleaning.png", "ic_category_sub_daily.png"),
    ("3d_flat_cleaning.png", "ic_category_sub_deep_cleaning.png", "ic_category_sub_deep.png"),
    ("3d_flat_service_appliance.png", "ic_category_sub_appliance_clean.png", "ic_category_sub_appliance.png"),
    ("3d_flat_service_nanny.png", "ic_category_sub_nanny_hourly.png", "ic_category_sub_nanny.png"),
    ("3d_flat_service_maternity.png", "ic_category_sub_maternity_childcare.png", "ic_category_sub_maternity.png"),
    ("3d_flat_service_new.png", "ic_category_sub_caregiving.png", "ic_category_sub_care.png"),
]

def main():
    print("Fixing housekeeping 3D icons...")
    for src, d1, d2 in copies:
        src_file = os.path.join(ASSETS_DIR, src)
        if not os.path.exists(src_file):
            src_file = os.path.join(ASSETS_DIR, "3d_flat_cleaning.png")
            
        dst1_file = os.path.join(DRAWABLE_DIR, d1)
        dst2_file = os.path.join(DRAWABLE_DIR, d2)
        
        shutil.copyfile(src_file, dst1_file)
        shutil.copyfile(src_file, dst2_file)
        print(f"Copied {src} -> {d1} & {d2}")

if __name__ == "__main__":
    main()
