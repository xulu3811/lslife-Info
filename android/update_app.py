path = 'app/src/main/java/com/lianshan/lslife/ui/LsLifeApp.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'import com.qingyuan.lslife.feature.settings.BindPhoneScreen' not in c:
    c = c.replace('import com.qingyuan.lslife.feature.settings.SettingsScreen', 'import com.qingyuan.lslife.feature.settings.SettingsScreen\nimport com.qingyuan.lslife.feature.settings.BindPhoneScreen')

c = c.replace('onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },\n                    onLoggedOut = {', 'onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },\n                    onOpenBindPhone = { navController.navigate(Routes.BIND_PHONE) },\n                    onLoggedOut = {')

if 'composable(Routes.BIND_PHONE)' not in c:
    block = """            composable(Routes.BIND_PHONE) {
                BindPhoneScreen(onBack = { navController.popBackStack() })
            }
"""
    c = c.replace('composable(Routes.ABOUT) {', block + '            composable(Routes.ABOUT) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated LsLifeApp.kt")
