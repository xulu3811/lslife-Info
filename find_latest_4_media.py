import os
import glob

d = r"C:\Users\xl246\.gemini\antigravity-ide\brain\1c50a577-6182-472b-be1c-29d41123f22e"
files = glob.glob(os.path.join(d, "media__*.*"))
files.sort(key=os.path.getmtime, reverse=True)
for f in files[:4]:
    print(os.path.basename(f), os.path.getmtime(f))
