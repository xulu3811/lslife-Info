import os
import io
from PIL import Image
from rembg import remove

OUTPUT_DIR = r"d:\LsLife\android\app\src\main\res\drawable"
INPUT_DIR = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b"

MAPPINGS = {
    "media__1786338866681.png": "ic_category_sub_appliance_clean_v4.webp",
    "media__1786338876229.jpg": "ic_category_sub_plumbing_v3.webp",
    "media__1786338927721.jpg": "ic_category_sub_fresh_fruit_v4.webp",
    "media__1786338946795.png": "ic_category_sub_moving_van_v6.webp",
}

def remove_bg(img):
    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='PNG')
    out_bytes = remove(img_byte_arr.getvalue())
    return Image.open(io.BytesIO(out_bytes)).convert('RGBA')

def process_image(filename, out_filename):
    input_path = os.path.join(INPUT_DIR, filename)
    print(f"Processing {input_path}...")
    
    img = Image.open(input_path).convert('RGBA')
    img_no_bg = remove_bg(img)
    
    # Get bounding box of non-transparent pixels
    bbox = img_no_bg.getbbox()
    if bbox:
        img_cropped = img_no_bg.crop(bbox)
    else:
        img_cropped = img_no_bg
        
    # We want to fit it nicely into a 512x512 canvas
    # Let's target a max dimension of 480 to have a small padding
    TARGET_MAX = 480
    w, h = img_cropped.size
    scale = TARGET_MAX / max(w, h)
    new_w, new_h = int(w * scale), int(h * scale)
    
    img_resized = img_cropped.resize((new_w, new_h), Image.Resampling.LANCZOS)
    
    # Paste into 512x512 transparent canvas
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    x = (512 - new_w) // 2
    y = (512 - new_h) // 2
    canvas.paste(img_resized, (x, y), img_resized)
    
    out_path = os.path.join(OUTPUT_DIR, out_filename)
    # Save as WebP
    canvas.save(out_path, format="WEBP", quality=90)
    print(f"Saved {out_filename}")

if __name__ == "__main__":
    for in_file, out_file in MAPPINGS.items():
        process_image(in_file, out_file)
    print("Done!")
