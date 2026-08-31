path = 'app/src/main/java/com/lianshan/lslife/ui/navigation/Routes.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'BIND_EMAIL = "bind_email"' not in c:
    c = c.replace('const val BIND_PHONE = "bind_phone"', 'const val BIND_PHONE = "bind_phone"\n    const val BIND_EMAIL = "bind_email"')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)
    print("Added BIND_EMAIL to Routes")
