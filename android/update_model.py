path = 'app/src/main/java/com/lianshan/lslife/core/model/Models.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('    val phone: String,', '    val phone: String,\n    val email: String? = null,')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Added email to User.kt")
