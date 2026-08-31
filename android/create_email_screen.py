path_phone = 'app/src/main/java/com/lianshan/lslife/feature/settings/BindPhoneScreen.kt'
path_email = 'app/src/main/java/com/lianshan/lslife/feature/settings/BindEmailScreen.kt'

with open(path_phone, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('BindPhoneScreen', 'BindEmailScreen')
c = c.replace('手机号', '邮箱')
c = c.replace('Icons.Outlined.PhoneAndroid', 'Icons.Outlined.Email')
c = c.replace('val phone = state.phone', 'val email = state.email')
c = c.replace('val isBound = !phone.isNullOrBlank()', 'val isBound = !email.isNullOrBlank()')
c = c.replace('val maskedPhone = if (phone!!.length >= 11) "${phone.substring(0, 3)}****${phone.substring(7)}" else phone',
              'val maskedEmail = if (email!!.indexOf("@") > 1) "${email.substring(0, 2)}***${email.substring(email.indexOf("@"))}" else email')
c = c.replace('text = maskedPhone,', 'text = maskedEmail,')

# Some wording tweaks for email
c = c.replace('邮箱可用于登录、身份验证和接收重要通知', '邮箱可用于接收电子账单、安全验证与活动通知')
c = c.replace('解绑后将无法使用该邮箱进行快捷登录，且部分实名及资金相关功能可能受限。', '解绑后您将无法通过该邮箱接收系统通知，且不可用于密码找回，请谨慎操作。')

with open(path_email, 'w', encoding='utf-8') as f:
    f.write(c)
print("Created BindEmailScreen.kt")
