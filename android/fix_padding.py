
import sys

file_path = "app/src/main/java/com/lianshan/lslife/feature/chat/ChatSessionListScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

text = text.replace(
    ".padding(horizontal = 16.dp, bottom = 16.dp)",
    ".padding(horizontal = 16.dp).padding(bottom = 16.dp)"
)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

