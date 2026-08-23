import re

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\home\HomeViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('loadMoments(page = 1)', '')
content = content.replace(', momentPosts = emptyList()', '')
content = re.sub(r'\s*fun loadMoreMoments\(\)\s*\{.*?\}', '', content, flags=re.DOTALL)
content = re.sub(r'\s*private fun loadMoments\(page: Int = 1\)\s*\{.*?\}\s*\}', '', content, flags=re.DOTALL)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\home\HomeViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("done")
