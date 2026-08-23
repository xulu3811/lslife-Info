import os
import io
from PIL import Image
from rembg import remove

source_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e\media__1786113148120.jpg"
target_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_parking_rent_v3.webp"

print(f"Processing {source_path} -> {target_path}")

with open(source_path, 'rb') as i:
    input_data = i.read()

output_data = remove(input_data)

img = Image.open(io.BytesIO(output_data)).convert("RGBA")

bbox = img.getbbox()
if bbox:
    img = img.crop(bbox)
    
img_resized = img.resize((256, 256), Image.Resampling.LANCZOS)

img_resized.save(target_path, format="WEBP", quality=85)
print(f"Saved {target_path}")
