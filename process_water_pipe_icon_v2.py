import os
import numpy as np
from PIL import Image, ImageFilter

source_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b\media__1786431960496.jpg"
target_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_plumbing_detail_v2.webp"

print(f"Loading {source_path}...")
img = Image.open(source_path).convert("RGBA")
arr = np.array(img, dtype=np.float32)

rgb = arr[:, :, :3]
# Calculate brightness / max channel
max_channel = np.max(rgb, axis=2)

# Black background mask: pixels with max brightness < 32 are pure background
alpha = np.zeros_like(max_channel)
# Soft transition for edge anti-aliasing between 25 and 45
alpha = np.clip((max_channel - 25.0) / 20.0 * 255.0, 0.0, 255.0)

arr[:, :, 3] = alpha
result = Image.fromarray(arr.astype(np.uint8))

# Crop to subject bounding box
bbox = result.getbbox()
if bbox:
    result = result.crop(bbox)
    print(f"Complete subject cropped bbox: {bbox}, size: {result.size}")

# Target canvas: 256 x 256
canvas_size = 256
target_subject_size = 224

w, h = result.size
aspect_ratio = w / h

if aspect_ratio > 1:
    new_w = target_subject_size
    new_h = int(new_w / aspect_ratio)
else:
    new_h = target_subject_size
    new_w = int(new_h * aspect_ratio)

print(f"Resizing to ({new_w}, {new_h}) while keeping full subject...")
img_resized = result.resize((new_w, new_h), Image.Resampling.LANCZOS)

# Create 256x256 transparent canvas
canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))

# Center horizontally and vertically
offset_x = (canvas_size - new_w) // 2
offset_y = (canvas_size - new_h) // 2

canvas.paste(img_resized, (offset_x, offset_y), img_resized)

os.makedirs(os.path.dirname(target_path), exist_ok=True)
canvas.save(target_path, format="WEBP", quality=90)
print(f"Saved processed icon to {target_path}")
print(f"Final icon bbox: {canvas.getbbox()}")
