from PIL import Image
import os
d = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e"
for f in os.listdir(d):
    if f.startswith("media__") or "meat" in f:
        try:
            img = Image.open(os.path.join(d, f))
            print(f"{f}: {img.size}")
        except:
            pass
