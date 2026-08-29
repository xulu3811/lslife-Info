
import sys

file_path = "app/src/main/java/com/lianshan/lslife/feature/profile/ProfileScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Top profile header padding
# Column(modifier = Modifier.padding(16.dp)) {
text = text.replace("Column(modifier = Modifier.padding(16.dp)) {", "Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {")

# 2. Top profile header spacer
# Spacer(modifier = Modifier.height(16.dp))
text = text.replace("Spacer(modifier = Modifier.height(16.dp))", "Spacer(modifier = Modifier.height(8.dp))")

# 3. M3StatItem padding
# modifier = Modifier.clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
text = text.replace("padding(horizontal = 16.dp, vertical = 8.dp)", "padding(horizontal = 12.dp, vertical = 4.dp)")

# 4. M3GroupCard title padding
# Column(modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)) {
text = text.replace("Column(modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)) {", "Column(modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) {")

# 5. Row padding inside M3GroupCard usages
# Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
text = text.replace("Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {", "Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {")

# 6. M3GridItem vertical padding
# modifier = modifier.then(Modifier.clickable { onClick() }.padding(vertical = 8.dp)),
text = text.replace("modifier = modifier.then(Modifier.clickable { onClick() }.padding(vertical = 8.dp)),", "modifier = modifier.then(Modifier.clickable { onClick() }.padding(vertical = 4.dp)),")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

