import sys
from PIL import Image
from rembg import remove
import os

def process_image(input_path, output_path):
    print(f"Processing {input_path}...")
    
    with open(input_path, 'rb') as i:
        input_data = i.read()
    
    # Remove background
    output_data = remove(input_data)
    
    from io import BytesIO
    img = Image.open(BytesIO(output_data)).convert('RGBA')
    
    # Get bounding box of non-transparent pixels
    bbox = img.getbbox()
    if not bbox:
        print(f"Warning: No bounding box found for {input_path}")
        return
        
    # Crop to bounding box
    img = img.crop(bbox)
    
    # Calculate resize ratio to fit within 396x396 (512 - 58*2)
    target_size = 512 - 58 * 2
    ratio = min(target_size / img.width, target_size / img.height)
    new_size = (int(img.width * ratio), int(img.height * ratio))
    
    # Resize with high quality
    img = img.resize(new_size, Image.Resampling.LANCZOS)
    
    # Create 512x512 transparent canvas
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    
    # Paste centered
    paste_x = (512 - new_size[0]) // 2
    paste_y = (512 - new_size[1]) // 2
    canvas.paste(img, (paste_x, paste_y), img)
    
    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    canvas.save(output_path, 'PNG')
    print(f"Saved {output_path}")

tasks = [
    ('C:/Users/xl246/.gemini/antigravity/brain/355bd04d-0523-43a1-bbce-8f47cc6c2188/.user_uploaded/media_1788069626207.png', 'd:/GitHub-lslife-V6.0/android/app/src/main/res/drawable/ic_category_carpool.png'),
    ('C:/Users/xl246/.gemini/antigravity/brain/355bd04d-0523-43a1-bbce-8f47cc6c2188/.user_uploaded/media_1788069628345.jpg', 'd:/GitHub-lslife-V6.0/android/app/src/main/res/drawable/ic_category_job.png'),
    ('C:/Users/xl246/.gemini/antigravity/brain/355bd04d-0523-43a1-bbce-8f47cc6c2188/.user_uploaded/media_1788069630362.jpg', 'd:/GitHub-lslife-V6.0/android/app/src/main/res/drawable/ic_category_life.png'),
    ('C:/Users/xl246/.gemini/antigravity/brain/355bd04d-0523-43a1-bbce-8f47cc6c2188/.user_uploaded/media_1788069690359.jpg', 'd:/GitHub-lslife-V6.0/android/app/src/main/res/drawable/ic_category_edu.png'),
    ('C:/Users/xl246/.gemini/antigravity/brain/355bd04d-0523-43a1-bbce-8f47cc6c2188/.user_uploaded/media_1788069692173.png', 'd:/GitHub-lslife-V6.0/android/app/src/main/res/drawable/ic_category_idle.png'),
]

for in_path, out_path in tasks:
    process_image(in_path, out_path)

