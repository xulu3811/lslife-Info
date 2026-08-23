import os

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                DataBoardItem(
                    label = "关注/粉丝", 
                    count = user?.followersCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFollowList
                )'''

replacement = '''                DataBoardItem(
                    label = "关注/粉丝", 
                    count = "${user?.followingCount ?: 0}/${user?.followersCount ?: 0}",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFollowList
                )'''

if target in content:
    content = content.replace(target, replacement)
    with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\ProfileScreen.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Replaced ProfileScreen count")
else:
    print("Target not found in ProfileScreen")

