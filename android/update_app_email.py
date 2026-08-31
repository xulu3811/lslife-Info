path = 'app/src/main/java/com/lianshan/lslife/ui/LsLifeApp.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'ChangeEmailScreen' not in c:
    c = c.replace('import com.qingyuan.lslife.feature.settings.ChangePhoneScreen', 'import com.qingyuan.lslife.feature.settings.ChangePhoneScreen\nimport com.qingyuan.lslife.feature.settings.ChangeEmailScreen')

    c = c.replace('BindEmailScreen(onBack = { navController.popBackStack() })', 'BindEmailScreen(onBack = { navController.popBackStack() }, onChangeEmail = { navController.navigate(Routes.CHANGE_EMAIL) })')

    block = '''            composable(Routes.CHANGE_EMAIL) {
                ChangeEmailScreen(onBack = { navController.popBackStack() })
            }
'''
    c = c.replace('composable(Routes.ABOUT) {', block + '            composable(Routes.ABOUT) {')

    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)
    print("Updated LsLifeApp")
