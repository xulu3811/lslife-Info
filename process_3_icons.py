import os
from PIL import Image
from rembg import remove

def process_image(input_path, output_path):
    print(f"Processing {input_path} -> {output_path}")
    
    with open(input_path, 'rb') as i:
        input_data = i.read()
        output_data = remove(input_data)
        
    with open(output_path + ".png", 'wb') as o:
        o.write(output_data)
        
    # Open, resize and save as WebP
    img = Image.open(output_path + ".png")
    
    # Crop to transparent bounding box
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    # Resize keeping aspect ratio
    max_size = 512
    img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
    
    # Save as webp
    img.save(output_path, "WEBP", lossless=False, quality=90)
    os.remove(output_path + ".png")
    print(f"Saved {output_path}")

base_dir = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b"
res_dir = r"d:\LsLife\android\app\src\main\res\drawable"

tasks = [
    (r"media__1786347458739.png", r"ic_category_sub_appliance_clean_v5.webp"),
    (r"media__1786347393711.jpg", r"ic_category_sub_plumbing_v4.webp"),
    (r"fresh_fruit_basket_1786347549501.png", r"ic_category_sub_fresh_fruit_v5.webp")
]

for in_file, out_file in tasks:
    process_image(os.path.join(base_dir, in_file), os.path.join(res_dir, out_file))
