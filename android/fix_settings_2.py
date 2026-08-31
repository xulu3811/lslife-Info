path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('    var confirmDeleteAccount by remember { mutableStateOf(false) }\n', '')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print('Cleaned up unused state variable')
