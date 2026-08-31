path = 'app/src/main/java/com/lianshan/lslife/feature/settings/SettingsScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Card Title
c = c.replace('fontSize = 15.sp,\n                fontWeight = FontWeight.Bold,\n                color = Color(0xFF374151),\n                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)', 
              'fontSize = 14.sp,\n                fontWeight = FontWeight.Bold,\n                color = Color(0xFF374151),\n                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp)')

# SettingsActionRow and Notification Row
c = c.replace('modifier = Modifier.size(22.dp)', 'modifier = Modifier.size(20.dp)')
c = c.replace('fontSize = 15.sp', 'fontSize = 14.sp')
c = c.replace('fontSize = 12.sp,\n                        color = Color(0xFF9CA3AF)', 'fontSize = 11.sp,\n                        color = Color(0xFF9CA3AF)')
c = c.replace('fontSize = 12.sp, \n                            color = Color(0xFF9CA3AF)', 'fontSize = 11.sp, \n                            color = Color(0xFF9CA3AF)')
c = c.replace('fontSize = 13.sp,\n                    color = Color(0xFF9CA3AF)', 'fontSize = 12.sp,\n                    color = Color(0xFF9CA3AF)')

# Chevron
c = c.replace('Icons.Filled.ChevronRight', 'Icons.AutoMirrored.Filled.ArrowForwardIos')
c = c.replace('modifier = Modifier.size(18.dp)', 'modifier = Modifier.size(14.dp)')

# Add import for ArrowForwardIos if not present
if 'ArrowForwardIos' not in c[:1000]:
    c = c.replace('import androidx.compose.material.icons.filled.ChevronRight', 'import androidx.compose.material.icons.filled.ChevronRight\nimport androidx.compose.material.icons.automirrored.filled.ArrowForwardIos')

# Logout button text
c = c.replace('fontSize = 15.sp,\n                    fontWeight = FontWeight.Bold,\n                    color = Color(0xFFEF4444)', 'fontSize = 14.sp,\n                    fontWeight = FontWeight.Bold,\n                    color = Color(0xFFEF4444)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("Rescaled UI elements")
