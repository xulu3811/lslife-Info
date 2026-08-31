import re

path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('import androidx.compose.material.icons.outlined.NoAccounts\n', 'import androidx.compose.material.icons.outlined.NoAccounts\nimport androidx.compose.material.icons.outlined.Email\nimport androidx.compose.material.icons.outlined.Lock\n')

old_block = """            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "手机号绑定",
                    subtitle = "更换或解绑手机号",
                    rightText = "138****0000",
                    onClick = { /* TODO */ }
                )
                SettingsActionRow(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "实名认证",
                    subtitle = "保障您的账号与资产安全",
                    rightText = "去认证",
                    onClick = { /* TODO */ }
                )
                SettingsActionRow(
                    icon = Icons.Outlined.NoAccounts,
                    title = "注销账号",
                    subtitle = "永久注销并清除所有数据",
                    onClick = { confirmDeleteAccount = true },
                    showDivider = false
                )
            }"""

new_block = """            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "手机号绑定",
                    subtitle = "更换或解绑手机号",
                    rightText = "138****0000",
                    onClick = { /* TODO */ }
                )
                SettingsActionRow(
                    icon = Icons.Outlined.Email,
                    title = "安全邮箱",
                    subtitle = "用于接收重要通知与账单",
                    rightText = "未设置",
                    onClick = { /* TODO */ }
                )
                SettingsActionRow(
                    icon = Icons.Outlined.Lock,
                    title = "修改密码",
                    subtitle = "定期更改密码以保障账号安全",
                    onClick = { /* TODO */ },
                    showDivider = false
                )
            }"""

if old_block in c:
    c = c.replace(old_block, new_block)
else:
    print('Error: Could not find block')

c = re.sub(r'    if \(confirmDeleteAccount\) \{[\s\S]*?\n    \}\n', '', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print('Done!')
