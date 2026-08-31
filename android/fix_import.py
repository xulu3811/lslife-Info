path = 'app/src/main/java/com/lianshan/lslife/feature/settings/BindEmailScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('import androidx.compose.material.icons.outlined.PhoneAndroid', 'import androidx.compose.material.icons.outlined.Email')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Fixed import in BindEmailScreen.kt")
