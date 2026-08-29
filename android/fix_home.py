
import sys
import re

file_path = "app/src/main/java/com/lianshan/lslife/feature/home/HomeScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Main background
text = text.replace(
    ".background(MaterialTheme.colorScheme.surfaceVariant)",
    ".background(androidx.compose.ui.graphics.Color(0xFFF3F5F8))"
)

# 2. PrimaryTabRow containerColor
text = text.replace(
    "containerColor = MaterialTheme.colorScheme.background,",
    "containerColor = androidx.compose.ui.graphics.Color.Transparent,"
)

# 3. KingkongItemView M3 Container
old_kingkong_image = """        AsyncImage(
            model = cat.iconUrl,
            contentDescription = cat.name,
            modifier = Modifier.size(48.dp) // Y,?Y+s48.dp
        )"""

new_kingkong_image = """        androidx.compose.material3.Surface(
            modifier = Modifier.size(54.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            color = androidx.compose.ui.graphics.Color(0xFFF0F4F9)
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = cat.iconUrl,
                    contentDescription = cat.name,
                    modifier = Modifier.size(40.dp)
                )
            }
        }"""
# In case encoding gets messed up, use regex.
text = re.sub(
    r"AsyncImage\(\s*model = cat\.iconUrl,\s*contentDescription = cat\.name,\s*modifier = Modifier\.size\(\d+\.dp\)[^\)]*\)",
    new_kingkong_image,
    text
)

# 4. StandardFeedCard shadow removal
shadow_pattern = r"\.shadow\(\s*elevation = \d+\.dp,\s*shape = RoundedCornerShape\(\d+\.dp\),\s*spotColor = Color\([xX\da-fA-F]+\),\s*ambientColor = Color\([xX\da-fA-F]+\)\s*\)"
text = re.sub(shadow_pattern, "", text)

# 5. Fix imports if necessary (Color is already imported generally, but we used fully qualified names to be safe)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

