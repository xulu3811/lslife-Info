
import sys

file_path = "app/src/main/java/com/lianshan/lslife/ui/components/PublishMenuBottomSheet.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

text = text.replace("disabledContainerColor = Color(0xFFFF4D4F).copy(alpha = 0.5f)", "disabledContainerColor = Color(0xFF4285F4).copy(alpha = 0.5f)")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

