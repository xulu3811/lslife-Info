import os

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import androidx.lifecycle.compose.collectAsStateWithLifecycle', 'import androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport androidx.lifecycle.repeatOnLifecycle')

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
