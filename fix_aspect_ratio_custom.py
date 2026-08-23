import os
import io
from PIL import Image
from rembg import remove

base_source = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b"
base_dest = r"d:\LsLife\android\app\src\main\res\drawable"

mappings = [
    ("media__1786359306832.png", "ic_category_sub_appliance_clean_v7.webp"),
    ("media__1786359312143.jpg", "ic_category_sub_house_sale_v5.webp")
]

CANVAS_SIZE = 256
MAX_CONTENT_SIZE = 220  # Leaves ~18px padding around edge for nice breathing room in circle

for src_name, dst_name in mappings:
    source_path = os.path.join(base_source, src_name)
    target_path = os.path.join(base_dest, dst_name)
    print(f"Processing {source_path} -> {target_path}")
    
    with open(source_path, 'rb') as i:
        input_data = i.read()
        
    output_data = remove(input_data)
    img = Image.open(io.BytesIO(output_data)).convert("RGBA")
    
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    
    # Calculate aspect-ratio preserving size
    w, h = img.size
    scale = min(MAX_CONTENT_SIZE / w, MAX_CONTENT_SIZE / h)
    new_w = int(w * scale)
    new_h = int(h * scale)
    
    img_resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)
    
    # Create 256x256 transparent square canvas
    canvas = Image.new("RGBA", (CANVAS_SIZE, CANVAS_SIZE), (0, 0, 0, 0))
    
    # Center paste
    offset_x = (CANVAS_SIZE - new_w) // 2
    offset_y = (CANVAS_SIZE - new_h) // 2
    canvas.paste(img_resized, (offset_x, offset_y), img_resized)
    
    canvas.save(target_path, format="WEBP", quality=95)
    print(f"Saved {target_path} (Original: {w}x{h} -> Scaled: {new_w}x{new_h}, Centered in 256x256)")

print("All done fixing icon aspect ratios!")
