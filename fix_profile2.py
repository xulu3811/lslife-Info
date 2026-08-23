import os

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    LaunchedEffect(Unit) { viewModel.load() }'''

replacement = '''    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            viewModel.load()
        }
    }'''

if target in content:
    content = content.replace(target, replacement)
    with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Replaced LaunchedEffect in ProfileScreen")
else:
    print("Target not found in ProfileScreen")

