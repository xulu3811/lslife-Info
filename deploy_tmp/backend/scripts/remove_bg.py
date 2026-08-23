import os
from rembg import remove
from PIL import Image
import io

def process_image(input_path, output_path):
    print(f"Processing {input_path}...")
    try:
        with open(input_path, 'rb') as i:
            input_data = i.read()
            
        # Remove background using rembg (U2-Net)
        output_data = remove(input_data)
        
        # Open the result and trim transparent pixels to avoid extra whitespace
        img = Image.open(io.BytesIO(output_data))
        bbox = img.getbbox()
        if bbox:
            img = img.crop(bbox)
            
        img.save(output_path, "PNG")
        print(f"Successfully saved to {output_path}")
    except Exception as e:
        print(f"Error processing {input_path}: {e}")

if __name__ == "__main__":
    # Example usage
    images_to_process = [
        {"in": "house_for_sale.jpg", "out": "3d_flat_house_sale.png"},
        {"in": "factory_building.jpg", "out": "3d_flat_factory.png"}
    ]
    
    for item in images_to_process:
        if os.path.exists(item["in"]):
            process_image(item["in"], item["out"])
        else:
            print(f"Input file {item['in']} not found.")
