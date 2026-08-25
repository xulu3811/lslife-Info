import os
import io
import glob
import argparse
from PIL import Image

def simple_remove_white_bg(img, tolerance=240):
    """Make white background transparent using floodfill from corners."""
    img = img.convert("RGBA")
    width, height = img.size
    pixels = img.load()
    
    # Find background color from top-left corner
    bg_color = pixels[0, 0]
    
    if bg_color[0] > tolerance and bg_color[1] > tolerance and bg_color[2] > tolerance:
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

def process_image(src_path, dest_dir, size, padding, method):
    filename = os.path.basename(src_path)
    name, _ = os.path.splitext(filename)
    dest_path = os.path.join(dest_dir, f"{name}.png")
    
    print(f"Processing: {src_path}")
    print(f"       -->: {dest_path}")
    
    try:
        # Create output dir if not exists
        os.makedirs(dest_dir, exist_ok=True)
        
        with open(src_path, 'rb') as f:
            input_bytes = f.read()
            
        if method == 'rembg':
            try:
                from rembg import remove
            except Exception as e:
                import traceback
                print("Error: 'rembg' import failed.")
                traceback.print_exc()
                print("Please install it or use '--method floodfill'.")
                return False
                
            print("  [rembg] Removing background...")
            output_bytes = remove(input_bytes)
            img = Image.open(io.BytesIO(output_bytes)).convert("RGBA")
        elif method == 'floodfill':
            print("  [floodfill] Removing white background...")
            img = Image.open(io.BytesIO(input_bytes)).convert("RGBA")
            img = simple_remove_white_bg(img)
        else:
            print("  [none] Skipping background removal...")
            img = Image.open(io.BytesIO(input_bytes)).convert("RGBA")
        
        bbox = img.getbbox()
        if not bbox:
            print(f"  Warning: Empty bounding box for {filename}, skipping crop.")
            cropped = img
        else:
            cropped = img.crop(bbox)
            
        max_subject_size = size - (padding * 2)
        
        cw, ch = cropped.size
        aspect_ratio = cw / ch
        
        if aspect_ratio > 1:
            new_w = max_subject_size
            new_h = int(new_w / aspect_ratio)
        else:
            new_h = max_subject_size
            new_w = int(new_h * aspect_ratio)
            
        resized = cropped.resize((new_w, new_h), Image.Resampling.LANCZOS)
        
        canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        
        offset_x = (size - new_w) // 2
        offset_y = (size - new_h) // 2
        
        canvas.paste(resized, (offset_x, offset_y), resized)
        
        canvas.save(dest_path, format="PNG", quality=100)
        print(f"  Done: Saved to {dest_path}")
        return True
        
    except Exception as e:
        print(f"  Error processing {src_path}: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description="LianShan Image Processing Pipeline Tool (rembg + Pillow bounding box crop)")
    parser.add_argument('-i', '--input', required=True, help="Input image file, directory, or glob pattern")
    parser.add_argument('-o', '--output', required=True, help="Output directory")
    parser.add_argument('--size', type=int, default=512, help="Target canvas size (default: 512)")
    parser.add_argument('--padding', type=int, default=24, help="Padding in pixels (default: 24)")
    parser.add_argument('--method', choices=['rembg', 'floodfill', 'none'], default='rembg', help="Background removal method (default: rembg)")
    
    args = parser.parse_args()
    
    files_to_process = []
    
    # Handle glob / directory / file
    if os.path.isdir(args.input):
        for ext in ('*.png', '*.jpg', '*.jpeg', '*.webp'):
            files_to_process.extend(glob.glob(os.path.join(args.input, ext)))
            files_to_process.extend(glob.glob(os.path.join(args.input, ext.upper())))
    elif '*' in args.input or '?' in args.input:
        files_to_process.extend(glob.glob(args.input))
    else:
        if os.path.isfile(args.input):
            files_to_process.append(args.input)
            
    files_to_process = list(set(files_to_process)) # remove duplicates
    
    if not files_to_process:
        print(f"No valid image files found for input: {args.input}")
        return
        
    print(f"Found {len(files_to_process)} file(s) to process.")
    print(f"Target size: {args.size}x{args.size}, Padding: {args.padding}, Method: {args.method}")
    print("-" * 50)
    
    success_count = 0
    for f in files_to_process:
        if process_image(f, args.output, args.size, args.padding, args.method):
            success_count += 1
            
    print("-" * 50)
    print(f"Processing complete! Successfully processed {success_count}/{len(files_to_process)} images.")

if __name__ == "__main__":
    main()
