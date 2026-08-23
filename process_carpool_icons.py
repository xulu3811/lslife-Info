import os
import io
from PIL import Image

def process_icon(source_path, target_path):
    print(f"Processing {source_path} -> {target_path}")
    
    img = Image.open(source_path).convert("RGBA")
    
    try:
        from rembg import remove
        with open(source_path, 'rb') as i:
            output_data = remove(i.read())
        img = Image.open(io.BytesIO(output_data)).convert("RGBA")
    except Exception as e:
        print(f"rembg warning: {e}")
    
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    
    img_resized = img.resize((256, 256), Image.Resampling.LANCZOS)
    img_resized.save(target_path, format="WEBP", quality=90)
    print(f"Successfully saved {target_path}")

# Image 1: Moving / Freight
source1 = r"C:\Users\xl246\.gemini\antigravity-ide\brain\ac1290e0-29d9-4878-848a-0385ab1094ce\media__1786325627861.png"
target1 = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_moving_van_v5.webp"
process_icon(source1, target1)

# Image 2: Bus / Construction Vehicle
source2 = r"C:\Users\xl246\.gemini\antigravity-ide\brain\ac1290e0-29d9-4878-848a-0385ab1094ce\media__1786325665412.png"
target2 = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_bus_construction_rent_v4.webp"
process_icon(source2, target2)
