path = 'app/src/main/java/com/lianshan/lslife/ui/navigation/Routes.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'BIND_PHONE = "bind_phone"' not in c:
    c = c.replace('const val SETTINGS = "settings"', 'const val SETTINGS = "settings"\n    const val BIND_PHONE = "bind_phone"')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)
    print("Added BIND_PHONE to Routes")
