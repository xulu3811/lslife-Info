
import sys
import re

file_path = "app/src/main/java/com/lianshan/lslife/feature/profile/ProfileScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Overall spacing
text = text.replace(".padding(16.dp),", ".padding(12.dp),", 1)
text = text.replace("verticalArrangement = Arrangement.spacedBy(16.dp)", "verticalArrangement = Arrangement.spacedBy(12.dp)", 1)
text = text.replace("Spacer(modifier = Modifier.height(24.dp))", "Spacer(modifier = Modifier.height(16.dp))")

# 2. Header
text = text.replace("shape = RoundedCornerShape(24.dp),", "shape = RoundedCornerShape(20.dp),")
text = text.replace(".size(64.dp)", ".size(56.dp)")
text = text.replace("fontSize = 20.sp,", "fontSize = 18.sp,")

# 3. M3GroupCard
text = text.replace("modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)", "modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)")
text = re.sub(r"fontSize = 16\.sp,", "fontSize = 15.sp,", text)

# 4. M3GridItem
text = text.replace("modifier = Modifier.size(44.dp)", "modifier = Modifier.size(38.dp)")
text = text.replace("modifier = Modifier.size(24.dp)", "modifier = Modifier.size(22.dp)")
text = text.replace("text = label, fontSize = 12.sp, color = Color(0xFF4B5563)", "text = label, fontSize = 11.sp, color = Color(0xFF4B5563)")

# 5. M3StatItem
text = text.replace("fontSize = 18.sp", "fontSize = 16.sp")
text = text.replace("text = label, fontSize = 12.sp, color = Color(0xFF6B7280)", "text = label, fontSize = 11.sp, color = Color(0xFF6B7280)")

# 6. M3MenuRow
text = text.replace("padding(horizontal = 16.dp, vertical = 14.dp)", "padding(horizontal = 16.dp, vertical = 12.dp)")
text = text.replace("modifier = Modifier.size(22.dp)", "modifier = Modifier.size(20.dp)")
text = text.replace("fontSize = 15.sp", "fontSize = 14.sp")
text = text.replace("fontSize = 13.sp", "fontSize = 12.sp")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

