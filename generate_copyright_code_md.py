import os

def collect_code(target_dir, ext_list):
    lines = []
    for root, dirs, files in os.walk(target_dir):
        for file in files:
            if any(file.endswith(ext) for ext in ext_list):
                filepath = os.path.join(root, file)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        for line in f:
                            line = line.rstrip()
                            stripped = line.strip()
                            if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
                                continue
                            if len(line) > 80:
                                line = line[:80] + "..."
                            lines.append(line)
                except Exception:
                    pass
    return lines

android_lines = collect_code("android/app/src/main/java", [".kt"])
backend_lines = collect_code("backend/src", [".ts"])

first_1500 = android_lines[:1500]
last_1500 = backend_lines[-1500:]
final_lines = first_1500 + last_1500

with open("清远同城_源代码鉴别材料_60页.md", "w", encoding="utf-8") as f:
    f.write("# 清远智慧同城生活服务平台 V1.0 - 源代码鉴别材料\n\n")
    f.write("此文件为提取的前连续30页与后连续30页源代码（每页50行，合计3000行）。\n\n")
    f.write("```text\n")
    f.write("\n".join(final_lines))
    f.write("\n```\n")

print("SUCCESS")
