from PIL import Image

path = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e\media__1786150372747.png"
img = Image.open(path)
print("Mode:", img.mode)
if img.mode == 'RGBA':
    alpha = img.getchannel('A')
    print("Min alpha:", alpha.getextrema()[0])
