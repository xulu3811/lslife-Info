import os
import shutil

ASSETS_DIR = r"d:\LsLife\backend\public\assets\icons"
DRAWABLE_DIR = r"d:\LsLife\android\app\src\main\res\drawable"

mapping = {
    "ic_category_sub_fresh_fruit.png": "3d_flat_fresh_food.png",
    "ic_category_sub_fresh_veg.png": "3d_flat_produce.png",
    "ic_category_sub_fresh_meat.png": "3d_flat_veg_meat.png",
    "ic_category_sub_fresh_seafood.png": "3d_flat_veg_fresh.png",
    "ic_category_sub_frozen_food.png": "3d_flat_veg_wholesale.png",
    "ic_category_sub_fresh_grocery.png": "3d_flat_veg_grocery.png",
    "ic_category_sub_fresh_deli.png": "3d_flat_dining.png",
}

def main():
    print("Fixing fresh food subcategory 3D icons...")
    for dst, src in mapping.items():
        src_path = os.path.join(ASSETS_DIR, src)
        dst_path = os.path.join(DRAWABLE_DIR, dst)
        shutil.copyfile(src_path, dst_path)
        print(f"Copied {src} -> {dst}")

if __name__ == "__main__":
    main()
