import os

seed_path = r"d:\LsLife\backend\prisma\seed.ts"
children_path = r"d:\LsLife\backend\cat_idle_children.txt"

with open(seed_path, 'r', encoding='utf-8') as f:
    seed_content = f.read()

with open(children_path, 'r', encoding='utf-8') as f:
    children_content = f.read()

# Find where to start replacing
start_marker = "    children: [\n      // 1.1 数码 3C"
end_marker = "  },\n  // 2. 房屋租售"

start_idx = seed_content.find("    children: [\n      // 1.1 数码 3C（核心高频交易区）")
if start_idx == -1:
    print("Could not find start marker, using fallback")
    start_idx = seed_content.find("    children: [\n      // 1.1")

end_idx = seed_content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print(f"Error finding markers. start_idx: {start_idx}, end_idx: {end_idx}")
    exit(1)

new_content = seed_content[:start_idx] + children_content + "\n" + seed_content[end_idx:]

with open(seed_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Successfully replaced children array in seed.ts")
