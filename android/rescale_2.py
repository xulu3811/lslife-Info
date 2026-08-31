path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('fontSize = 13.sp,\n                    fontWeight = if (isSelected)', 'fontSize = 12.sp,\n                    fontWeight = if (isSelected)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Rescaled segmented control")
