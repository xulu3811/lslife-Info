import re

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace moment_publish routing in PublishMenuBottomSheet
content = content.replace(
    """                    if (routeId.startsWith("moment_publish")) {
                        navController.navigate(routeId)
                    } else {
                        navController.navigate(Routes.publish(null, routeId)) 
                    }""",
    """                    navController.navigate(Routes.publish(null, routeId))"""
)

# Remove the MOMENT_PUBLISH composable block
content = re.sub(
    r'\s*composable\(\s*route = Routes\.MOMENT_PUBLISH,.*?\}\s*\)\s*\{.*?com\.lianshan\.lslife\.feature\.publish\.MomentPublishScreen\(.*?\)\s*\}',
    '',
    content,
    flags=re.DOTALL
)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\LsLifeApp.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("LsLifeApp.kt updated!")
