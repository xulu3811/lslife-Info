import os
import io
from PIL import Image
from rembg import remove

base_source = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e"
base_dest = r"d:\LsLife\android\app\src\main\res\drawable"

mappings = [
    ("fresh_fruit_icon_1786108371855.png", "ic_category_sub_fresh_fruit_v3.webp"),
    ("fresh_veg_icon_1786108392357.png", "ic_category_sub_fresh_veg_v3.webp"),
    ("fresh_meat_icon_1786148004602.png", "ic_category_sub_fresh_meat_v3.webp"),
    ("fresh_seafood_icon_1786108406149.png", "ic_category_sub_fresh_seafood_v3.webp"),
    ("frozen_food_icon_1786108424968.png", "ic_category_sub_frozen_food_v3.webp"),
    ("fresh_grocery_icon_1786108438182.png", "ic_category_sub_fresh_grocery_v3.webp"),
    ("fresh_deli_icon_1786108448000.png", "ic_category_sub_fresh_deli_v3.webp")
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
        
    # Resize preserving aspect ratio
    img.thumbnail((256, 256), Image.Resampling.LANCZOS)
    
    # Save the correctly proportioned image
    img.save(target_path, format="WEBP", quality=85)
    print(f"Saved {target_path}")

print("All done!")
