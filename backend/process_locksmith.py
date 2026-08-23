import os
import io
from PIL import Image
from rembg import remove

src = r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787405653199.jpg"
dest = r"android\app\src\main\res\drawable\ic_category_sub_locksmith.png"

try:
    with open(src, 'rb') as f:
        input_bytes = f.read()
        
    print("Removing background for locksmith...")
    output_bytes = remove(input_bytes)
    img = Image.open(io.BytesIO(output_bytes)).convert("RGBA")
    
    bbox = img.getbbox()
    if bbox:
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
        
        canvas.save(dest, format="PNG", quality=100)
        print(f"Saved: {dest}")
except Exception as e:
    print(f"Error: {e}")
