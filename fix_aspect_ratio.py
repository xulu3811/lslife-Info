import os
import io
from PIL import Image
from rembg import remove

base_source = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e"
base_dest = r"d:\LsLife\android\app\src\main\res\drawable"

mappings = [
    ("media__1786144264078.png", "ic_category_sub_daily_cleaning_v3.webp"),
    ("media__1786144266705.png", "ic_category_sub_deep_cleaning_v3.webp"),
    ("media__1786144399740.jpg", "ic_category_sub_appliance_clean_v3.webp"),
    ("media__1786144641079.jpg", "ic_category_sub_nanny_hourly_v3.webp"),
    ("media__1786144647092.jpg", "ic_category_sub_maternity_childcare_v3.webp"),
    ("media__1786145333945.png", "ic_category_sub_caregiving_v3.webp")
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
        
    # Resize preserving aspect ratio!
    img.thumbnail((256, 256), Image.Resampling.LANCZOS)
    
    # Save the correctly proportioned image
    img.save(target_path, format="WEBP", quality=85)
    print(f"Saved {target_path}")

print("All done!")
