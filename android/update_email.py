path_phone = 'app/src/main/java/com/lianshan/lslife/feature/settings/ChangePhoneScreen.kt'
path_email = 'app/src/main/java/com/lianshan/lslife/feature/settings/ChangeEmailScreen.kt'

with open(path_phone, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('ChangePhoneScreen', 'ChangeEmailScreen')
c = c.replace('更换手机号', '更换邮箱')
c = c.replace('新手机号', '新邮箱')
c = c.replace('手机号', '邮箱')
c = c.replace('newPhone', 'newEmail')
c = c.replace('KeyboardType.Phone', 'KeyboardType.Email')
c = c.replace('Icons.Outlined.PhoneAndroid', 'Icons.Outlined.Email')
c = c.replace('newEmail.length == 11', 'newEmail.contains("@")')
c = c.replace('newEmail.length != 11', '!newEmail.contains("@")')

with open(path_email, 'w', encoding='utf-8') as f:
    f.write(c)
print("Created ChangeEmailScreen")
