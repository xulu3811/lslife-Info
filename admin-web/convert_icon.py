from PIL import Image
import sys

img = Image.open(r'C:\Users\xl246\.gemini\antigravity\brain\a865ecb9-e608-43b5-8e14-8268e4731cc3\.user_uploaded\media_1788154036274.jpg').convert('RGBA')

# Create square version if not square, by padding or cropping
size = min(img.size)
img = img.crop((0, 0, size, size))
img.save(r'd:\GitHub-lslife-V6.0\admin-web\public\favicon.png', format='PNG')
img.resize((64, 64)).save(r'd:\GitHub-lslife-V6.0\admin-web\public\favicon.ico', format='ICO')
