import urllib.request
import urllib.parse
import os
import time

icons = {
    "pt_temp": "daily settlement or temporary worker, a calendar with a coin",
    "pt_promo": "flyer distribution or promotion, a stack of flyers",
    "pt_hotel": "catering or room service part-time, a serving tray",
    "pt_tutor": "tutoring or art training, a blackboard and a palette",
    "pt_errand": "errand or proxy service, running fast with a package"
}

base_prompt = "A beautiful 3D flat iOS style icon for {}, vibrant colors, premium design, minimalist, soft lighting, isolated on solid white background, highly detailed, centered"
output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../public/assets/icons"))

for name, desc in icons.items():
    filename = f"3d_flat_{name}.png"
    filepath = os.path.join(output_dir, filename)
    if os.path.exists(filepath):
        print(f"Skipping {filename} - already exists")
        continue

    prompt = base_prompt.format(desc)
    encoded_prompt = urllib.parse.quote(prompt)
    url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width=512&height=512&nologo=true"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    
    print(f"Generating {filename}...")
    for attempt in range(2):
        try:
            with urllib.request.urlopen(req, timeout=10) as response, open(filepath, 'wb') as out_file:
                out_file.write(response.read())
            print(f"  -> Saved {filename}")
            break
        except Exception as e:
            print(f"  -> Failed attempt {attempt+1}: {e}")
            time.sleep(2)
    time.sleep(2)
