import sys
import glob
import os
from rembg import remove

def process_images(input_pattern, output_dir):
    print(f"Searching for images matching: {input_pattern}")
    files = glob.glob(input_pattern)
    if not files:
        print("No files found!")
        return

    os.makedirs(output_dir, exist_ok=True)
    print(f"Found {len(files)} files to process.")

    for input_path in files:
        try:
            filename = os.path.basename(input_path)
            # Remove the timestamp from the filename if present
            # Original: ic_category_3c_1786027580739.png -> ic_category_3c.png
            parts = filename.split("_")
            if len(parts) >= 4 and parts[-1].split(".")[0].isdigit():
                base_name = "_".join(parts[:-1]) + ".png" 
            else:
                base_name = filename
                
            output_path = os.path.join(output_dir, base_name)

            print(f"Processing: {filename} -> {base_name}")
            with open(input_path, 'rb') as i:
                with open(output_path, 'wb') as o:
                    input_data = i.read()
                    # Apply rembg, keeping the alpha channel
                    output_data = remove(input_data)
                    o.write(output_data)
            print(f"Successfully saved to {output_path}")
        except Exception as e:
            print(f"Failed to process {input_path}: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python process_icons.py <input_dir> <output_dir>")
        sys.exit(1)
    artifact_dir = sys.argv[1]
    res_dir = sys.argv[2]
    
    input_pattern = os.path.join(artifact_dir, "ic_category_*.png")
    process_images(input_pattern, res_dir)
    
    print("All image processing complete.")
