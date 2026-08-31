from PIL import Image

def process_image(input_path, output_path):
    # Open image
    img = Image.open(input_path).convert('RGBA')
    
    # Get bounding box of non-transparent pixels
    bbox = img.getbbox()
    if not bbox:
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
    canvas.paste(img, (paste_x, paste_y))
    
    # Save
    canvas.save(output_path, 'PNG')

process_image('C:/Users/xl246/.gemini/antigravity/brain/355bd04d-0523-43a1-bbce-8f47cc6c2188/.user_uploaded/media_1788067499833.png', 'd:/GitHub-lslife-V6.0/android/app/src/main/res/drawable/ic_category_sale.png')
