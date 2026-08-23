import os
from PIL import Image

source_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e\media__1786150372747.png"
target_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_daily_cleaning_v4.webp"

print(f"Processing {source_path} -> {target_path}")

img = Image.open(source_path).convert("RGBA")

# Crop to bounding box of content (removes extra empty space)
bbox = img.getbbox()
if bbox:
    img = img.crop(bbox)

# Resize keeping aspect ratio
img.thumbnail((256, 256), Image.Resampling.LANCZOS)

img.save(target_path, format="WEBP", quality=85)
print(f"Saved {target_path}")
