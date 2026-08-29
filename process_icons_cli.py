import argparse
import os
import io
import sys
from pathlib import Path
from PIL import Image

try:
    from rembg import remove
except ImportError:
    print("Error: rembg is not installed. Please run: pip install rembg pillow")
    sys.exit(1)

def process_image(input_path: str, output_path: str, padding: int = 58, target_size: int = 512):
    """
    Process a single image:
    1. Remove background
    2. Crop to bounding box
    3. Resize and pad to target_size x target_size with RGBA transparent background
    """
    print(f"Processing: {input_path}")
    
    with open(input_path, 'rb') as f:
        input_data = f.read()
        
    # 1. Remove background
    try:
        output_data = remove(input_data)
        img = Image.open(io.BytesIO(output_data)).convert("RGBA")
    except Exception as e:
        print(f"  [!] Failed to remove background for {input_path}: {e}")
        return False
        
    # 2. Crop to bounding box
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    else:
        print(f"  [!] No content found after background removal for {input_path}")
        return False
        
    # 3. Resize proportionally
    # Calculate available space
    max_content_size = target_size - (padding * 2)
    
    # Calculate aspect ratio preserving dimensions
    aspect_ratio = img.width / img.height
    if aspect_ratio > 1:
        new_width = max_content_size
        new_height = int(max_content_size / aspect_ratio)
    else:
        new_height = max_content_size
        new_width = int(max_content_size * aspect_ratio)
        
    img_resized = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
    
    # 4. Create transparent base and paste
    final_img = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
    paste_x = (target_size - new_width) // 2
    paste_y = (target_size - new_height) // 2
    
    final_img.paste(img_resized, (paste_x, paste_y), img_resized)
    
    # Save
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    final_img.save(output_path, format="PNG")
    print(f"  [+] Saved to: {output_path}")
    return True

def main():
    parser = argparse.ArgumentParser(
        description="同城清远 - AI 视觉处理管线 CLI 工具\n"
                    "自动对实物图进行去背 (rembg) -> 包围盒极限裁剪 -> 等比缩放并注入呼吸感留白 -> 输出 512x512 RGBA PNG。",
        formatter_class=argparse.RawTextHelpFormatter
    )
    
    parser.add_argument("-i", "--input", required=True, help="输入路径 (单张图片或包含图片的文件夹)")
    parser.add_argument("-o", "--output", required=True, help="输出路径 (单张图片输出路径或目标文件夹)")
    parser.add_argument("-p", "--padding", type=int, default=58, help="四周留白尺寸 (默认: 58px)")
    parser.add_argument("-s", "--size", type=int, default=512, help="输出图片尺寸 (默认: 512px)")
    
    args = parser.parse_args()
    
    input_path = Path(args.input)
    output_path = Path(args.output)
    
    if not input_path.exists():
        print(f"Error: Input path '{input_path}' does not exist.")
        sys.exit(1)
        
    if input_path.is_file():
        # Process single file
        # If output is a directory that exists (or has no suffix), treat as dir
        if output_path.is_dir() or not output_path.suffix:
            output_file = output_path / f"{input_path.stem}_processed.png"
        else:
            output_file = output_path
        process_image(str(input_path), str(output_file), args.padding, args.size)
        
    elif input_path.is_dir():
        # Process directory
        output_path.mkdir(parents=True, exist_ok=True)
        valid_extensions = {'.png', '.jpg', '.jpeg', '.webp'}
        
        success_count = 0
        total_count = 0
        
        for file in input_path.iterdir():
            if file.is_file() and file.suffix.lower() in valid_extensions:
                total_count += 1
                out_file = output_path / f"{file.stem}.png"
                if process_image(str(file), str(out_file), args.padding, args.size):
                    success_count += 1
                    
        print(f"\nBatch processing complete! Successfully processed {success_count}/{total_count} images.")

if __name__ == "__main__":
    main()
