import os
import glob
from PIL import Image
import rembg

def process_images(directory):
    # Find all 3d_flat_*.png in the directory
    pattern = os.path.join(directory, "3d_flat_*.png")
    files = glob.glob(pattern)
    
    for file_path in files:
        print(f"Processing {file_path}...")
        try:
            # 1. Remove background
            with open(file_path, "rb") as input_file:
                input_data = input_file.read()
            
            output_data = rembg.remove(input_data)
            
            # Write temp to read with PIL
            temp_path = file_path + ".tmp.png"
            with open(temp_path, "wb") as output_file:
                output_file.write(output_data)
                
            # 2. Crop to bounding box
            img = Image.open(temp_path).convert("RGBA")
            bbox = img.getbbox()
            if bbox:
                # Crop to bbox
                img = img.crop(bbox)
                # Resize if needed? Android Coil will fit it automatically
                # Let's just save it back
                img.save(file_path, "PNG")
                print(f"  -> Cropped and saved to {file_path}")
            else:
                print(f"  -> Empty bounding box for {file_path}")
                
            os.remove(temp_path)
        except Exception as e:
            print(f"Error processing {file_path}: {e}")

if __name__ == "__main__":
    icon_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../public/assets/icons"))
    process_images(icon_dir)
