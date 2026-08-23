import urllib.request
import urllib.parse

prompt = "A beautiful 3D flat iOS style icon for apartment renting, cozy room, vibrant colors, premium design, minimalist, soft lighting, isolated on solid white background, highly detailed, centered"
encoded_prompt = urllib.parse.quote(prompt)
url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width=512&height=512&nologo=true"

req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req) as response, open("test_pollinations.png", 'wb') as out_file:
        out_file.write(response.read())
    print("Success!")
except Exception as e:
    print(f"Failed: {e}")
