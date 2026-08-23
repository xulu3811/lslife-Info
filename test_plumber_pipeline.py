import os
import io
from PIL import Image
from rembg import remove, new_session

img_path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\7e30b89d-8f18-44ad-9358-44a37f5f608b\media__1786431960496.jpg"

with open(img_path, 'rb') as f:
    input_bytes = f.read()

# 1. u2net_human_seg
session_human = new_session("u2net_human_seg")
out_human = remove(input_bytes, session=session_human)
img_human = Image.open(io.BytesIO(out_human)).convert("RGBA")
print("u2net_human_seg bbox:", img_human.getbbox())
img_human.save(r"d:\LsLife\test_human_seg.png")

# 2. bgr thresholding on dark background
img_orig = Image.open(img_path).convert("RGBA")
# Combine human seg mask + color thresholding for metal pipes/sink!
# Let's inspect pixels of the pipe at (0..200, 600..1024)
