path = 'app/src/main/java/com/lianshan/lslife/ui/LsLifeApp.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'import com.qingyuan.lslife.feature.settings.ChangePasswordScreen' not in c:
    c = c.replace('import com.qingyuan.lslife.feature.settings.BindEmailScreen', 'import com.qingyuan.lslife.feature.settings.BindEmailScreen\nimport com.qingyuan.lslife.feature.settings.ChangePasswordScreen')

c = c.replace('onOpenBindEmail = { navController.navigate(Routes.BIND_EMAIL) },\n                    onLoggedOut = {', 'onOpenBindEmail = { navController.navigate(Routes.BIND_EMAIL) },\n                    onOpenChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },\n                    onLoggedOut = {')

if 'composable(Routes.CHANGE_PASSWORD)' not in c:
    block = """            composable(Routes.CHANGE_PASSWORD) {
                ChangePasswordScreen(
                    onBack = { navController.popBackStack() },
                    onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
                )
            }
"""
    c = c.replace('composable(Routes.ABOUT) {', block + '            composable(Routes.ABOUT) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Updated LsLifeApp.kt for password")
