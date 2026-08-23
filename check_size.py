from PIL import Image
try:
    img = Image.open(r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_fresh_meat.webp")
    print("ic_category_sub_fresh_meat.webp size:", img.size)
except Exception as e:
    print(e)
