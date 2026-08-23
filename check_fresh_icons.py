from PIL import Image
import os
d = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e"
for f in ["fresh_fruit_icon_1786108371855.png",
          "fresh_veg_icon_1786108392357.png",
          "fresh_seafood_icon_1786108406149.png",
          "frozen_food_icon_1786108424968.png",
          "fresh_grocery_icon_1786108438182.png",
          "fresh_deli_icon_1786108448000.png"]:
    try:
        img = Image.open(os.path.join(d, f))
        print(f"{f}: {img.size}")
    except:
        pass
