import os
import io
from PIL import Image
from rembg import remove

source_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\ac1290e0-29d9-4878-848a-0385ab1094ce\media__1786325665412.png"
target_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_bus_construction_rent_v6.webp"

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
target_subject_width = 248  # Increased from 224 to 248 for larger visual fill

w, h = img.size
aspect_ratio = w / h

new_w = target_subject_width
new_h = int(new_w / aspect_ratio)

print(f"Resizing enlarged bus to ({new_w}, {new_h}) maintaining aspect ratio...")
img_resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)

# Create 256x256 transparent canvas
canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))

# Center horizontally and vertically
offset_x = (canvas_size - new_w) // 2
offset_y = (canvas_size - new_h) // 2

canvas.paste(img_resized, (offset_x, offset_y), img_resized)

os.makedirs(os.path.dirname(target_path), exist_ok=True)
canvas.save(target_path, format="WEBP", quality=90)
print(f"Saved enlarged bus icon to {target_path}")
print(f"Final icon bbox: {canvas.getbbox()}")
