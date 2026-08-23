import os
import io
from PIL import Image
from rembg import remove

base_source = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b"
base_dest = r"d:\LsLife\android\app\src\main\res\drawable"

mappings = [
    ("media__1786359306832.png", "ic_category_sub_appliance_clean_v6.webp"),
    ("media__1786359312143.jpg", "ic_category_sub_house_sale_v4.webp")
]

for src_name, dst_name in mappings:
    source_path = os.path.join(base_source, src_name)
    target_path = os.path.join(base_dest, dst_name)
    print(f"Processing {source_path} -> {target_path}")
    
    with open(source_path, 'rb') as i:
        input_data = i.read()
        
    output_data = remove(input_data)
    img = Image.open(io.BytesIO(output_data)).convert("RGBA")
    
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    # Resize to 256x256
    img_resized = img.resize((256, 256), Image.Resampling.LANCZOS)
    img_resized.save(target_path, format="WEBP", quality=90)
    print(f"Saved {target_path}")

print("All done processing icons!")
