path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsViewModel.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('val phone: String? = null,', 'val phone: String? = null,\n    val email: String? = null,')
c = c.replace('phone = user?.phone', 'phone = user?.phone,\n                        email = user?.email')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated SettingsViewModel.kt")
