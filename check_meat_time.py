import os
import time

f = r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_fresh_meat.webp"
if os.path.exists(f):
    mtime = os.path.getmtime(f)
    print("ic_category_sub_fresh_meat.webp modified:", time.ctime(mtime))
