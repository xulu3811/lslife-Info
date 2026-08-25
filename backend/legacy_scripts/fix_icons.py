import os
import glob
from PIL import Image

def process_icon(filepath):
    try:
        img = Image.open(filepath).convert("RGBA")
        bbox = img.getbbox()
        if not bbox:
            print(f"Skipping {filepath} (empty bbox)")
            return
            
        # Crop to the exact content
        cropped = img.crop(bbox)
        
        # Target size for Android icons (512x512)
        canvas_size = 512
        # Leave a tiny breathing room, e.g., 24px on each side -> max subject size 464
        padding = 24
        max_subject_size = canvas_size - (padding * 2)
        
        cw, ch = cropped.size
        aspect_ratio = cw / ch
        
        if aspect_ratio > 1:
            # Wider than tall
            new_w = max_subject_size
            new_h = int(new_w / aspect_ratio)
        else:
            # Taller than wide
            new_h = max_subject_size
            new_w = int(new_h * aspect_ratio)
            
        resized = cropped.resize((new_w, new_h), Image.Resampling.LANCZOS)
        
        # Create new blank canvas
        canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
        
        # Center it
        offset_x = (canvas_size - new_w) // 2
        offset_y = (canvas_size - new_h) // 2
        
        # Check if we should align bottom (useful for people/buildings)
        # But for generic icons, centering is safer. Let's do center for now, 
        # unless it looks bad. Actually, bottom alignment is often better for objects sitting on a floor.
        # Let's use center for perfect visual balance in circular frames.
        
        canvas.paste(resized, (offset_x, offset_y), resized)
        
        canvas.save(filepath, format="PNG", quality=100)
        print(f"Processed: {os.path.basename(filepath)}")
    except Exception as e:
        print(f"Error processing {filepath}: {e}")

# Target directories and files
target_dir = r"android\app\src\main\res\drawable"
icons_to_process = [
    # Main categories
    "ic_category_carpool.png", "ic_category_edu.png", "ic_category_fresh.png", 
    "ic_category_idle.png", "ic_category_job.png", "ic_category_life.png", 
    "ic_category_rent.png", "ic_category_repair.png", "ic_category_sale.png", "ic_category_service.png",
    
    # Sub categories for Housekeeping
    "ic_category_sub_appliance_clean_v10.png", "ic_category_sub_caregiving_v3.png", 
    "ic_category_sub_daily_cleaning_v4.png", "ic_category_sub_deep_cleaning_v4.png", 
    "ic_category_sub_maternity_childcare_v3.png", "ic_category_sub_nanny_hourly_v3.png"
]

for icon_name in icons_to_process:
    filepath = os.path.join(target_dir, icon_name)
    if os.path.exists(filepath):
        process_icon(filepath)
    else:
        print(f"File not found: {filepath}")

print("Done processing icons!")
