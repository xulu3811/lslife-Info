import re

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\navigation\Routes.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'\s*const val MOMENT_PUBLISH\s*=\s*"moment_publish\?topic=\{topic\}&momentType=\{momentType\}"', '', content)

content = re.sub(r'\s*fun momentPublish\(topic: String\? = null, momentType: String\? = null\): String \{.*?\n\s*\}', '', content, flags=re.DOTALL)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\navigation\Routes.kt', 'w', encoding='utf-8') as f:
    f.write(content)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt', 'r', encoding='utf-8') as f:
    app_content = f.read()

# Remove the MOMENT_PUBLISH composable block from NavHost
app_content = re.sub(r'\s*composable\(\s*route = Routes\.MOMENT_PUBLISH,.*?\}\s*\)\s*\{.*?com\.lianshan\.lslife\.feature\.publish\.MomentPublishScreen\(.*?\)\s*\}', '', app_content, flags=re.DOTALL)

# Remove the BottomBar visibility condition for moment_publish
app_content = re.sub(r'\s*if\s*\(routeId\.startsWith\("moment_publish"\)\)\s*\{\s*return@let\s*false\s*\}', '', app_content)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt', 'w', encoding='utf-8') as f:
    f.write(app_content)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\home\HomeViewModel.kt', 'r', encoding='utf-8') as f:
    home_content = f.read()

home_content = re.sub(r'\s*val momentPosts:\s*List<Post>\s*=\s*emptyList\(\),?', '', home_content)
home_content = re.sub(r'\s*val momentPage:\s*Int\s*=\s*1,?', '', home_content)
home_content = re.sub(r'\s*val momentHasMore:\s*Boolean\s*=\s*true,?', '', home_content)
home_content = re.sub(r'\s*val momentLoadingMore:\s*Boolean\s*=\s*false,?', '', home_content)

home_content = re.sub(r'momentPosts\s*=\s*emptyList\(\),?', '', home_content)
home_content = re.sub(r'\s*loadMoments\(page = 1\)', '', home_content)
home_content = re.sub(r'\s*fun loadMoreMoments\(\)\s*\{.*?\}', '', home_content, flags=re.DOTALL)
home_content = re.sub(r'\s*private fun loadMoments\(page: Int = 1\)\s*\{.*?\}\s*\}', '', home_content, flags=re.DOTALL)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\home\HomeViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(home_content)

print("Updated Routes.kt, LsLifeApp.kt, and HomeViewModel.kt")
