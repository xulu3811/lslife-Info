import os
import io
from PIL import Image

# Image 1: Moving Van
img1_src = r"C:\Users\xl246\.gemini\antigravity-ide\brain\ac1290e0-29d9-4878-848a-0385ab1094ce\media__1786325627861.png"
img1_dst = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_moving_van_v4.webp"

# Image 2: Dump truck / Construction rent
img2_src = r"C:\Users\xl246\.gemini\antigravity-ide\brain\ac1290e0-29d9-4878-848a-0385ab1094ce\media__1786325665412.png"
img2_dst = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_bus_construction_rent_v3.webp"

def process_image(src, dst):
    print(f"Processing {src} -> {dst}")
    img = Image.open(src).convert("RGBA")
    try:
        from rembg import remove
        with open(src, 'rb') as i:
            output_data = remove(i.read())
        img = Image.open(io.BytesIO(output_data)).convert("RGBA")
    except Exception as e:
        print(f"rembg warning: {e}")

    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)

    img_resized = img.resize((256, 256), Image.Resampling.LANCZOS)
    img_resized.save(dst, format="WEBP", quality=90)
    print(f"Successfully saved {dst}")

process_image(img1_src, img1_dst)
process_image(img2_src, img2_dst)
