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

# The mapping based on the user's instructions:
# 1. 水电/管道 -> media_1787405912406.jpg -> ic_category_sub_plumbing_detail_v2.png
# 2. 开锁/换锁 -> media_1787405912406.jpg (tempmediaStorage) -> ic_category_sub_locksmith.png
# 3. 家电维修 -> media_1787405912415.jpg -> ic_category_sub_repair_v2.png
# 4. 房屋修缮 -> media_1787405912428.jpg -> ic_category_sub_renovation_v2.png
# 5. 数码/电脑维修 -> media_1787405912429.jpg -> ic_category_sub_digital_repair_v2.png

mappings = [
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787405912406.jpg", "ic_category_sub_plumbing_detail_v2.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\tempmediaStorage\media_1787405912406.jpg", "ic_category_sub_locksmith.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787405912415.jpg", "ic_category_sub_repair_v2.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787405912428.jpg", "ic_category_sub_renovation_v2.png"),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787405912429.jpg", "ic_category_sub_digital_repair_v2.png")
]

for src, dest in mappings:
    process_and_save(src, dest)

print("Done processing maintenance icons!")
