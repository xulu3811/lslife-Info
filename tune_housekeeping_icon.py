import os
import io
from PIL import Image
from rembg import remove

source_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b\media__1786359306832.png"
target_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_appliance_clean_v8.webp"

with open(source_path, 'rb') as i:
    input_data = i.read()

output_data = remove(input_data)
img = Image.open(io.BytesIO(output_data)).convert("RGBA")

bbox = img.getbbox()
if bbox:
    img = img.crop(bbox)

w, h = img.size

# We want the bucket (on the right) to be fully visible and centered inside the circle.
# Enlarge content size to 240 (filling canvas vertically)
CANVAS_SIZE = 256
TARGET_H = 242

scale = TARGET_H / h
new_w = int(w * scale)
new_h = int(h * scale)

img_resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)

# Create 256x256 canvas
canvas = Image.new("RGBA", (CANVAS_SIZE, CANVAS_SIZE), (0, 0, 0, 0))

# Shift LEFT by ~25px so the cleaning bucket on the right moves into the center of the circle!
# Standard center offset_x would be (256 - new_w) // 2
standard_offset_x = (CANVAS_SIZE - new_w) // 2
shift_left = 22  # Shift left to bring bucket in
offset_x = max(2, standard_offset_x - shift_left)
offset_y = (CANVAS_SIZE - new_h) // 2 + 4

canvas.paste(img_resized, (offset_x, offset_y), img_resized)
canvas.save(target_path, format="WEBP", quality=95)

print(f"Saved {target_path} (Original: {w}x{h} -> Scaled: {new_w}x{new_h}, offset: {offset_x}, {offset_y})")
