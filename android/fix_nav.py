
import sys

file_path = "app/src/main/java/com/lianshan/lslife/ui/LsLifeApp.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Remove forced height 60.dp from NavigationBar to allow M3 pill to render properly
text = text.replace(
    "NavigationBar(\n                        modifier = Modifier.height(60.dp),", 
    "NavigationBar(\n                        // modifier = Modifier.height(60.dp), removed to let M3 pill show properly"
)

# 2. Update Publish + button to M3 Squircle
text = text.replace(
    """                                            modifier = Modifier.size(44.dp),
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            shadowElevation = 6.dp""",
    """                                            modifier = Modifier.size(48.dp),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            shadowElevation = 0.dp"""
)

# 3. Enhance colors slightly (M3 typical)
text = text.replace(
    "indicatorColor = androidx.compose.ui.graphics.Color(0xFFE8F0FE)",
    "indicatorColor = androidx.compose.ui.graphics.Color(0xFFD3E3FD)" # More prominent blue pill
)
text = text.replace(
    "selectedIconColor = androidx.compose.ui.graphics.Color(0xFF1A73E8)",
    "selectedIconColor = androidx.compose.ui.graphics.Color(0xFF041E49)" # Darker blue icon inside pill
)


with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

