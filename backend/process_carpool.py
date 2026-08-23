import os
import io
from PIL import Image
from rembg import remove

def process_and_save(src_path, dest_name):
    dest_path = os.path.join(r"android\app\src\main\res\drawable", dest_name)
    print(f"Processing {src_path} -> {dest_path}")
    
    try:
        with open(src_path, 'rb') as f:
            input_bytes = f.read()
            
        print("Removing background...")
        output_bytes = remove(input_bytes)
        img = Image.open(io.BytesIO(output_bytes)).convert("RGBA")
        
        bbox = img.getbbox()
        if not bbox:
            print("Empty bbox, skipping.")
            return
            
        cropped = img.crop(bbox)
        
        canvas_size = 512
        padding = 24
        max_subject_size = canvas_size - (padding * 2)
        
        cw, ch = cropped.size
        aspect_ratio = cw / ch
        
        if aspect_ratio > 1:
            new_w = max_subject_size
            new_h = int(new_w / aspect_ratio)
        else:
            new_h = max_subject_size
            new_w = int(new_h * aspect_ratio)
            
        resized = cropped.resize((new_w, new_h), Image.Resampling.LANCZOS)
        
        canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
        
        offset_x = (canvas_size - new_w) // 2
        offset_y = (canvas_size - new_h) // 2
        
        canvas.paste(resized, (offset_x, offset_y), resized)
        
        canvas.save(dest_path, format="PNG", quality=100)
        print(f"Saved: {dest_path}")
        
    except Exception as e:
        print(f"Error processing {src_path}: {e}")

mappings = [
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787446122640.png", "ic_category_sub_carpool_v2.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787446124917.jpg", "ic_category_sub_passenger_v3.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787446126537.png", "ic_category_sub_freight_v2.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787446131012.png", "ic_category_sub_moving_van_v6.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787446135458.png", "ic_category_sub_car_rental_v3.png")
]

for src, dest in mappings:
    process_and_save(src, dest)

print("Done processing carpool/rental icons!")
