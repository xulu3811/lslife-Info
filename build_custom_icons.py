import os
import sys
import requests
import io
from PIL import Image, ImageDraw, ImageFont
from rembg import remove

sys.stdout.reconfigure(encoding='utf-8')

OUTPUT_DIR = r"d:\LsLife\android\app\src\main\res\drawable"

def download_image(url):
    headers = {'User-Agent': 'Mozilla/5.0'}
    response = requests.get(url, headers=headers, timeout=15)
    response.raise_for_status()
    return Image.open(io.BytesIO(response.content)).convert('RGBA')

def remove_bg(img):
    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='PNG')
    out_bytes = remove(img_byte_arr.getvalue())
    return Image.open(io.BytesIO(out_bytes)).convert('RGBA')

def create_rounded_badge(text, bg_color=(229, 57, 53, 235), text_color=(255, 255, 255, 255), size=(120, 48)):
    badge = Image.new('RGBA', size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(badge)
    draw.rounded_rectangle([(0, 0), (size[0]-1, size[1]-1)], radius=12, fill=bg_color)
    
    try:
        font = ImageFont.truetype("msyh.ttc", 22)
    except:
        try:
            font = ImageFont.truetype("simhei.ttf", 22)
        except:
            font = ImageFont.load_default()
            
    bbox = draw.textbbox((0, 0), text, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    tx = (size[0] - tw) / 2
    ty = (size[1] - th) / 2 - 2
    draw.text((tx, ty), text, font=font, fill=text_color)
    return badge

def build_fresh_icon():
    print("Building fresh icon...")
    url_basket = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=600&auto=format&fit=crop"
    url_steak = "https://images.unsplash.com/photo-1603048588665-791ca8aea617?w=500&auto=format&fit=crop"
    
    img_basket = download_image(url_basket)
    bg_removed_basket = remove_bg(img_basket)
    
    img_steak = download_image(url_steak)
    bg_removed_steak = remove_bg(img_steak)
    
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    basket_resized = bg_removed_basket.resize((380, 380), Image.Resampling.LANCZOS)
    canvas.paste(basket_resized, (30, 40), basket_resized)
    
    steak_resized = bg_removed_steak.resize((220, 180), Image.Resampling.LANCZOS)
    canvas.paste(steak_resized, (250, 280), steak_resized)
    
    canvas.save(os.path.join(OUTPUT_DIR, "ic_category_fresh.png"))
    print("Saved ic_category_fresh.png")

def build_rent_icon():
    print("Building rent icon...")
    url_room = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600&auto=format&fit=crop"
    img_room = download_image(url_room)
    
    room_cropped = img_room.resize((420, 360), Image.Resampling.LANCZOS)
    
    mask = Image.new('L', (420, 360), 0)
    draw_mask = ImageDraw.Draw(mask)
    draw_mask.rounded_rectangle([(0, 0), (420, 360)], radius=36, fill=255)
    
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    canvas.paste(room_cropped, (46, 70), mask)
    
    badge = create_rounded_badge("出租", bg_color=(229, 57, 53, 240), size=(130, 54))
    canvas.paste(badge, (30, 40), badge)
    
    canvas.save(os.path.join(OUTPUT_DIR, "ic_category_rent.png"))
    print("Saved ic_category_rent.png")

def build_carpool_icon():
    print("Building carpool icon...")
    url_car = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=700&auto=format&fit=crop"
    img_car = download_image(url_car)
    bg_removed_car = remove_bg(img_car)
    
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    car_resized = bg_removed_car.resize((460, 320), Image.Resampling.LANCZOS)
    canvas.paste(car_resized, (26, 120), car_resized)
    
    badge = create_rounded_badge("拼车/租车", bg_color=(25, 118, 210, 240), size=(160, 52))
    canvas.paste(badge, (300, 70), badge)
    
    canvas.save(os.path.join(OUTPUT_DIR, "ic_category_carpool.png"))
    print("Saved ic_category_carpool.png")

def build_job_icon():
    print("Building job icon...")
    url_agent = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=600&auto=format&fit=crop"
    img_agent = download_image(url_agent)
    bg_removed_agent = remove_bg(img_agent)
    
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    agent_resized = bg_removed_agent.resize((420, 460), Image.Resampling.LANCZOS)
    canvas.paste(agent_resized, (46, 30), agent_resized)
    
    canvas.save(os.path.join(OUTPUT_DIR, "ic_category_job.png"))
    print("Saved ic_category_job.png")

def build_life_icon():
    print("Building life icon...")
    url_dining = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop"
    img_dining = download_image(url_dining)
    bg_removed_dining = remove_bg(img_dining)
    
    canvas = Image.new('RGBA', (512, 512), (0, 0, 0, 0))
    dining_resized = bg_removed_dining.resize((440, 420), Image.Resampling.LANCZOS)
    canvas.paste(dining_resized, (36, 46), dining_resized)
    
    canvas.save(os.path.join(OUTPUT_DIR, "ic_category_life.png"))
    print("Saved ic_category_life.png")

if __name__ == "__main__":
    try:
        build_fresh_icon()
        build_rent_icon()
        build_carpool_icon()
        build_job_icon()
        build_life_icon()
        print("All custom icons generated successfully!")
    except Exception as e:
        print(f"Error: {e}")
