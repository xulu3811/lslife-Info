import os

search_dir = r"d:\LsLife"
for root, dirs, files in os.walk(search_dir):
    for f in files:
        if "meat" in f.lower() or "fresh" in f.lower():
            print(os.path.join(root, f))
