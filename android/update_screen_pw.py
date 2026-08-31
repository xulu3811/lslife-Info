path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('onOpenBindEmail: () -> Unit = {},', 'onOpenBindEmail: () -> Unit = {},\n    onOpenChangePassword: () -> Unit = {},')

c = c.replace('title = "修改密码",\n                    subtitle = "定期更改密码以保障账号安全",\n                    rightText = "",\n                    onClick = { /* TODO */ }',
              'title = "修改密码",\n                    subtitle = "定期更改密码以保障账号安全",\n                    rightText = "",\n                    onClick = onOpenChangePassword')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated SettingsScreen.kt for password")
