import os
import glob
from PIL import Image

def main():
    drawable_dir = r"d:\GitHub-lslife-V6.0\android\app\src\main\res\drawable"
    
    # Find all image files
    image_files = []
    image_files.extend(glob.glob(os.path.join(drawable_dir, "*.png")))
    image_files.extend(glob.glob(os.path.join(drawable_dir, "*.webp")))
    image_files.extend(glob.glob(os.path.join(drawable_dir, "*.jpg")))
    
    print(f"Found {len(image_files)} image files to compress.")
    
    total_original_size = 0
    total_optimized_size = 0
    
    for img_path in image_files:
        try:
            original_size = os.path.getsize(img_path)
            total_original_size += original_size
            
            with Image.open(img_path) as img:
                # Target: Max 200x200
                max_size = 200
                img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
                
                base_name = os.path.splitext(os.path.basename(img_path))[0]
                output_path = os.path.join(drawable_dir, base_name + ".webp")
                
                # Save as highly compressed WEBP
                img.save(output_path, format="WEBP", quality=55, method=6)
                
            # Remove original if it's not the same file
            if img_path != output_path and os.path.exists(img_path):
                os.remove(img_path)
                
            optimized_size = os.path.getsize(output_path)
            total_optimized_size += optimized_size
                
        except Exception as e:
            print(f"Error compressing {img_path}: {e}")
            total_optimized_size += os.path.getsize(img_path)

    print(f"Total size before: {total_original_size / 1024 / 1024:.2f} MB")
    print(f"Total size after : {total_optimized_size / 1024 / 1024:.2f} MB")
    print(f"Space saved      : {(total_original_size - total_optimized_size) / 1024 / 1024:.2f} MB")

if __name__ == "__main__":
    main()
