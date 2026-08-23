import os
import io
from PIL import Image
from rembg import remove, new_session

img_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b\media__1786431960496.jpg"

with open(img_path, 'rb') as f:
    input_bytes = f.read()

# 1. Standard u2net with alpha matting
out_matting = remove(
    input_bytes,
    alpha_matting=True,
    alpha_matting_foreground_threshold=240,
    alpha_matting_background_threshold=15,
    alpha_matting_erode_size=5
)
img_matting = Image.open(io.BytesIO(out_matting)).convert("RGBA")
print("u2net + alpha matting bbox:", img_matting.getbbox())

# 2. isnet-general-use model
try:
    session_isnet = new_session("isnet-general-use")
    out_isnet = remove(input_bytes, session=session_isnet)
    img_isnet = Image.open(io.BytesIO(out_isnet)).convert("RGBA")
    print("isnet-general-use bbox:", img_isnet.getbbox())
except Exception as e:
    print("isnet error:", e)

# 3. u2net_human_seg model
try:
    session_human = new_session("u2net_human_seg")
    out_human = remove(input_bytes, session=session_human)
    img_human = Image.open(io.BytesIO(out_human)).convert("RGBA")
    print("u2net_human_seg bbox:", img_human.getbbox())
except Exception as e:
    print("u2net_human_seg error:", e)
