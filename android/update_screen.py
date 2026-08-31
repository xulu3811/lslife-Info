path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('onOpenBindPhone: () -> Unit = {},', 'onOpenBindPhone: () -> Unit = {},\n    onOpenBindEmail: () -> Unit = {},')

c = c.replace('val maskedPhone = state.phone?.let { if (it.length >= 11) "${it.substring(0, 3)}****${it.substring(7)}" else it } ?: "未绑定"', 
              'val maskedPhone = state.phone?.let { if (it.length >= 11) "${it.substring(0, 3)}****${it.substring(7)}" else it } ?: "未绑定"\n    val maskedEmail = state.email?.let { if (it.indexOf("@") > 1) "${it.substring(0, 2)}***${it.substring(it.indexOf("@"))}" else it } ?: "未设置"')

c = c.replace('title = "安全邮箱",\n                    subtitle = "用于接收重要通知与账单",\n                    rightText = "未设置",\n                    onClick = { /* TODO */ }',
              'title = "安全邮箱",\n                    subtitle = "用于接收重要通知与账单",\n                    rightText = maskedEmail,\n                    onClick = onOpenBindEmail')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated SettingsScreen.kt")
