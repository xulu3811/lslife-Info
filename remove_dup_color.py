import os

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
color_import_count = 0
for line in lines:
    if line.strip() == "import androidx.compose.ui.graphics.Color":
        if color_import_count == 0:
            color_import_count += 1
            new_lines.append(line)
    else:
        new_lines.append(line)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\PublicProfileScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

