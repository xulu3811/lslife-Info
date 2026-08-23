from PIL import Image
import numpy as np

src_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b\media__1786359306832.png"
dst_path = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_appliance_clean_v9.webp"

img = Image.open(src_path).convert("RGBA")
print(f"Source size: {img.size}, mode: {img.mode}")

# Convert to numpy array to check background
data = np.array(img)
r, g, b, a = data[:, :, 0], data[:, :, 1], data[:, :, 2], data[:, :, 3]

# Check if background is pure white or light grey
# Near white: R > 248, G > 248, B > 248
white_mask = (r > 245) & (g > 245) & (b > 245)

# Set alpha to 0 where background is white
data[:, :, 3][white_mask] = 0

cleaned_img = Image.fromarray(data, mode="RGBA")

# Bounding box of non-zero alpha
bbox = cleaned_img.getbbox()
print(f"BBox: {bbox}")

if bbox:
    cropped = cleaned_img.crop(bbox)

w, h = cropped.size
print(f"Cropped dimensions: {w}x{h}")

# Target scale: fit within 240px height inside 256x256
CANVAS_SIZE = 256
TARGET_H = 236

scale = TARGET_H / h
new_w = int(w * scale)
new_h = int(h * scale)

img_resized = cropped.resize((new_w, new_h), Image.Resampling.LANCZOS)

# Create 256x256 transparent canvas
canvas = Image.new("RGBA", (CANVAS_SIZE, CANVAS_SIZE), (0, 0, 0, 0))

# Shift left slightly so the bucket on the right is in full view inside the 60.dp circle
standard_offset_x = (CANVAS_SIZE - new_w) // 2
offset_x = max(0, standard_offset_x - 14)
offset_y = (CANVAS_SIZE - new_h) // 2 + 6

canvas.paste(img_resized, (offset_x, offset_y), img_resized)
canvas.save(dst_path, format="WEBP", quality=95)

print(f"Saved {dst_path} (Scaled: {new_w}x{new_h}, offset: {offset_x}, {offset_y})")
