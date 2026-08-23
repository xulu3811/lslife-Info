import os
with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace authLabel
content = content.replace('if (!item.authLabel.isNullOrEmpty()) {', 'if (!item.identityType.isNullOrEmpty()) {')
content = content.replace('Text(item.authLabel,', 'Text(item.identityType,')

# Remove Button completely
import re
button_pattern = r'Button\(\s*onClick = \{ viewModel\.toggleFollow\(item\.id\) \}[\s\S]*?\}\s*\)'
content = re.sub(button_pattern, '', content)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Removed Button and fixed identityType")
