import os
import io
from PIL import Image
from rembg import remove

base_source = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e"
base_dest = r"d:\LsLife\android\app\src\main\res\drawable"

mappings = [
    ("media__1786273614241.png", "ic_category_sub_3c_v2.webp"),
    ("media__1786273873821.jpg", "ic_category_sub_clothing_v2.webp"),
    ("media__1786273890382.jpg", "ic_category_sub_home_appliances_v2.webp"),
    ("media__1786273901379.jpg", "ic_category_sub_beauty_v2.webp"),
    ("media__1786273930110.png", "ic_category_sub_sports_mobility_v2.webp")
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
        
    img.thumbnail((256, 256), Image.Resampling.LANCZOS)
    
    img.save(target_path, format="WEBP", quality=85)
    print(f"Saved {target_path}")

print("All done!")
