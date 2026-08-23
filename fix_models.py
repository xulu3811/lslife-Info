import os

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\core\model\Models.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    val followersCount: Int = 0,
    val favoritesCount: Int = 0,'''

replacement = '''    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val favoritesCount: Int = 0,'''

if target in content:
    content = content.replace(target, replacement)
    with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\core\model\Models.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Replaced User model")
else:
    print("Target not found in Models")

