from PIL import Image
import glob
import os

def optimize_icons():
    drawable_dir = r"d:\LsLife\android\app\src\main\res\drawable"
    pattern = os.path.join(drawable_dir, "ic_category_*.png")
    png_files = glob.glob(pattern)
    
    print(f"Found {len(png_files)} PNG icons to optimize.")
    
    for png_path in png_files:
        try:
            with Image.open(png_path) as img:
                # Resize to 256x256 with high quality resampling
                img_resized = img.resize((256, 256), Image.Resampling.LANCZOS)
                
                # Create output path with .webp extension
                webp_path = os.path.splitext(png_path)[0] + ".webp"
                
                # Save as WEBP
                img_resized.save(webp_path, format="WEBP", quality=85)
                
            # Remove original PNG
            os.remove(png_path)
            print(f"Optimized: {os.path.basename(webp_path)}")
        except Exception as e:
            print(f"Error optimizing {png_path}: {e}")

if __name__ == "__main__":
    optimize_icons()
