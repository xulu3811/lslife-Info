from PIL import Image
import numpy as np

img = Image.open(r"d:\LsLife\android\app\src\main\res\drawable\ic_category_sub_appliance_clean_v9.webp")
data = np.array(img)

# Print non-transparent colors and bucket area
# Bucket is near bottom right: Y range 150-230, X range 120-220
bucket_crop = data[150:230, 120:220]
non_transparent = np.sum(bucket_crop[:, :, 3] > 0)
print(f"Bucket area non-transparent pixel count: {non_transparent}")

# Sample bucket blue color (R < 50, G > 100, B > 180)
blue_pixels = np.sum((bucket_crop[:, :, 0] < 100) & (bucket_crop[:, :, 2] > 150) & (bucket_crop[:, :, 3] > 200))
print(f"Bucket blue pixels count: {blue_pixels}")

yellow_pixels = np.sum((bucket_crop[:, :, 0] > 180) & (bucket_crop[:, :, 1] > 180) & (bucket_crop[:, :, 2] < 100) & (bucket_crop[:, :, 3] > 200))
print(f"Bottle yellow pixels count: {yellow_pixels}")

print("v9 verification complete!")
