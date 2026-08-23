import os
from rembg import remove

def process(input_path, output_path):
    print(f"Processing {input_path}")
    with open(input_path, 'rb') as i:
        with open(output_path, 'wb') as o:
            o.write(remove(i.read()))
    print(f"Saved to {output_path}")

input_1 = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e\local_life_icon_1786102756292.png"
out_1 = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_life.png"

input_2 = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e\gourmet_dining_icon_1786102774492.png"
out_2 = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_gourmet_dining.png"

process(input_1, out_1)
process(input_2, out_2)
