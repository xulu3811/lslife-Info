import os
from PIL import Image
import glob

def optimize_all_icons():
    drawable_dir = r"d:\GitHub-lslife-V6.0\android\app\src\main\res\drawable"
    
    # 查找所有的 png 和 webp 文件
    image_files = []
    image_files.extend(glob.glob(os.path.join(drawable_dir, "*.png")))
    image_files.extend(glob.glob(os.path.join(drawable_dir, "*.webp")))
    
    print(f"Found {len(image_files)} image files to optimize.")
    
    total_original_size = 0
    total_optimized_size = 0
    optimized_count = 0
    
    for img_path in image_files:
        try:
            original_size = os.path.getsize(img_path)
            total_original_size += original_size
            
            with Image.open(img_path) as img:
                # 检查尺寸，如果超过 256x256，则等比例缩小
                max_size = 256
                width, height = img.size
                needs_resize = width > max_size or height > max_size
                
                # 如果是 PNG，或者尺寸过大，或者虽然是 WEBP 但体积较大(>15KB)，我们都重新压缩
                needs_recompress = img_path.lower().endswith('.png') or needs_resize or original_size > 15 * 1024
                
                if not needs_recompress:
                    total_optimized_size += original_size
                    continue
                
                if needs_resize:
                    img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
                
                # 构建输出路径 (强制后缀为 .webp)
                base_name = os.path.splitext(os.path.basename(img_path))[0]
                output_path = os.path.join(drawable_dir, base_name + ".webp")
                
                # 保存为 WEBP 格式 (保留透明通道，质量设置为 80)
                img.save(output_path, format="WEBP", quality=80, method=6)
            
            # 如果原始文件是 png 且成功输出了 webp，则删除原 png
            if img_path.lower().endswith('.png') and os.path.exists(output_path):
                os.remove(img_path)
                
            optimized_size = os.path.getsize(output_path)
            total_optimized_size += optimized_size
            optimized_count += 1
            
            # 如果本身就是 webp，但被重新压缩了，原始文件会被覆盖，这里做个日志
            print(f"Optimized: {base_name} ({original_size/1024:.1f}KB -> {optimized_size/1024:.1f}KB)")
                
        except Exception as e:
            print(f"Error optimizing {img_path}: {e}")
            total_optimized_size += os.path.getsize(img_path)

    print("-" * 30)
    print(f"Optimization complete! Processed {optimized_count} files.")
    print(f"Total size before: {total_original_size / 1024 / 1024:.2f} MB")
    print(f"Total size after : {total_optimized_size / 1024 / 1024:.2f} MB")
    print(f"Space saved      : {(total_original_size - total_optimized_size) / 1024 / 1024:.2f} MB")

if __name__ == "__main__":
    optimize_all_icons()
