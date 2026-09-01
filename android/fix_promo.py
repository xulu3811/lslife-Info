import re

file_path = r"d:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\profile\PromotionCenterScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("color = MaterialTheme.colorScheme.onTertiaryContainer,\n                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)", "color = MaterialTheme.colorScheme.surface,\n                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)")

content = re.sub(r'(Text\(\s*"[^"]+",\s*fontSize = .*?,\s*fontWeight = .*?,\s*color = )MaterialTheme.colorScheme.onTertiaryContainer', r'\1MaterialTheme.colorScheme.onPrimary', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
