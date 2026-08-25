import os
from PIL import Image, ImageDraw

def simple_remove_white_bg(img, tolerance=240):
    """Make white background transparent using floodfill from corners."""
    img = img.convert("RGBA")
    
    # Create a mask for flood fill
    # We will flood fill from the 4 corners
    width, height = img.size
    
    # Simple thresholding: if pixel is very bright/white, make it transparent
    # But only if it connects to the edge. A simple way in PIL is to use ImageDraw.floodfill
    # but PIL floodfill works on colors, not ranges easily.
    
    # Alternative: Just make all white-ish pixels transparent?
    # This might make white parts of the appliances transparent.
    # Let's do a strict white removal for the exact background color.
    
    pixels = img.load()
    
    # Find background color from top-left corner
    bg_color = pixels[0, 0]
    
    # If it's a solid background (e.g., pure white)
    if bg_color[0] > tolerance and bg_color[1] > tolerance and bg_color[2] > tolerance:
        # We will manually do a BFS floodfill to find all connected background pixels
        visited = set()
        queue = [(0,0), (width-1, 0), (0, height-1), (width-1, height-1)]
        
        while queue:
            x, y = queue.pop(0)
            if (x, y) in visited:
                continue
            if x < 0 or x >= width or y < 0 or y >= height:
                continue
                
            p = pixels[x, y]
            if p[0] > tolerance and p[1] > tolerance and p[2] > tolerance and p[3] > 0:
                pixels[x, y] = (255, 255, 255, 0)
                visited.add((x, y))
                queue.extend([(x+1, y), (x-1, y), (x, y+1), (x, y-1)])
                
    return img

def process_without_rembg(src_path, dest_name, do_floodfill=False):
    dest_path = os.path.join(r"android\app\src\main\res\drawable", dest_name)
    print(f"Processing {src_path} -> {dest_path}")
    
    try:
        img = Image.open(src_path).convert("RGBA")
        
        if do_floodfill:
            img = simple_remove_white_bg(img, tolerance=245)
            
        # Get bounding box of non-transparent pixels
        bbox = img.getbbox()
        if bbox:
            img = img.crop(bbox)
            
        canvas_size = 512
        padding = 24
        max_subject_size = canvas_size - (padding * 2)
        
        cw, ch = img.size
        aspect_ratio = cw / ch
        
        if aspect_ratio > 1:
            new_w = max_subject_size
            new_h = int(new_w / aspect_ratio)
        else:
            new_h = max_subject_size
            new_w = int(new_h * aspect_ratio)
            
        resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)
        
        canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
        
        offset_x = (canvas_size - new_w) // 2
        offset_y = (canvas_size - new_h) // 2
        
        canvas.paste(resized, (offset_x, offset_y), resized)
        
        canvas.save(dest_path, format="PNG", quality=100)
        print(f"Saved: {dest_path}")
        
    except Exception as e:
        print(f"Error processing {src_path}: {e}")

mappings = [
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787458581202.png", "ic_category_sub_3c_v2.png", False),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787458594782.jpg", "ic_category_sub_home_appliances_v2.png", True),
    (r"C:\Users\xl246\.gemini\antigravity\brain\dae46f90-79c3-4613-b709-01137a0b6df9\.user_uploaded\media_1787457029903.jpg", "ic_category_sub_renovation_v2.png", True)
]

for src, dest, do_fill in mappings:
    process_without_rembg(src, dest, do_fill)

print("Done processing fix icons!")
