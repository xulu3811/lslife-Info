import os
import glob
from rembg import remove
from PIL import Image

def process_image(input_path):
    print(f"Processing {input_path}...")
    try:
        with open(input_path, 'rb') as i:
            input_data = i.read()
            
        output_data = remove(input_data)
        
        # Save temporarily
        temp_out = input_path + ".tmp.png"
        with open(temp_out, 'wb') as o:
            o.write(output_data)
            
        # Crop the output
        img = Image.open(temp_out)
        img = img.crop(img.getbbox())
        
        # Save back to original path, enforcing transparent PNG
        img.save(input_path, "PNG")
        os.remove(temp_out)
        print(f"  -> Cropped and saved to {input_path}")
    except Exception as e:
        print(f"  -> Error: {e}")

icon_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../public/assets/icons"))
png_files = glob.glob(os.path.join(icon_dir, '3d_flat_*.png'))

for filepath in png_files:
    # Only process if file size is small (meaning it's likely the downloaded JPEG instead of our large transparent PNGs)
    if os.path.getsize(filepath) < 150000:
        process_image(filepath)
    else:
        print(f"Skipping {filepath} (size > 150KB, assumed already processed)")
