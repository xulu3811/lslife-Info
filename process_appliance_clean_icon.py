import os
import io
from PIL import Image
from rembg import remove

source_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b\media__1786432471877.jpg"
target_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_appliance_clean_v10.webp"

print(f"Reading {source_path}...")
with open(source_path, 'rb') as f:
    input_bytes = f.read()

print("Removing background with rembg...")
output_bytes = remove(input_bytes)
img = Image.open(io.BytesIO(output_bytes)).convert("RGBA")

# Crop to subject bounding box
bbox = img.getbbox()
if bbox:
    img = img.crop(bbox)
    print(f"Cropped bbox: {bbox}, size: {img.size}")

# Target canvas: 256 x 256
canvas_size = 256
target_subject_size = 224

w, h = img.size
aspect_ratio = w / h

if aspect_ratio > 1:
    new_w = target_subject_size
    new_h = int(new_w / aspect_ratio)
else:
    new_h = target_subject_size
    new_w = int(new_h * aspect_ratio)

img_resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)

# Create 256x256 transparent canvas
canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))

# Paste centered horizontally and vertically
offset_x = (canvas_size - new_w) // 2
offset_y = (canvas_size - new_h) // 2

canvas.paste(img_resized, (offset_x, offset_y), img_resized)

os.makedirs(os.path.dirname(target_path), exist_ok=True)
canvas.save(target_path, format="WEBP", quality=90)
print(f"Saved processed icon to {target_path}")
print(f"Final icon bbox: {canvas.getbbox()}")
