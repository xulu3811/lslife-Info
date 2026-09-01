import re

path = r'd:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\wallet\WalletScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Remove the "1 virtualCoinName = 1.00元" text block
text_to_remove = r'''                        Text(
                            "1 \ = 1.00元",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )'''
content = content.replace(text_to_remove, '')

# 2. Replace the old badge UI with a cleaner M3 badge UI
old_badge = r'''            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }'''
            
new_badge = r'''            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }'''
content = content.replace(old_badge, new_badge)

# 3. Replace the '+ 赠 10' text color in the card to be more subtle (M3 primary instead of error)
old_bonus = r'''color = MaterialTheme.colorScheme.error
                    )
                } else {'''
new_bonus = r'''color = MaterialTheme.colorScheme.primary
                    )
                } else {'''
content = content.replace(old_bonus, new_bonus)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Replaced!")
