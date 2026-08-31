path = 'app/src/main/java/com/lianshan/lslife/ui/navigation/Routes.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'CHANGE_PASSWORD = "change_password"' not in c:
    c = c.replace('const val BIND_EMAIL = "bind_email"', 'const val BIND_EMAIL = "bind_email"\n    const val CHANGE_PASSWORD = "change_password"')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)
    print("Added CHANGE_PASSWORD to Routes")
