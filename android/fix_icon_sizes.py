import sys
import re

def modify_file(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        text = f.read()

    # Container size
    text = text.replace("Modifier.size(54.dp)", "Modifier.size(60.dp)")
    # Corner shape
    text = text.replace("RoundedCornerShape(18.dp)", "RoundedCornerShape(20.dp)")
    # Image size
    text = text.replace("Modifier.size(40.dp)", "Modifier.size(46.dp)")
    
    # In HomeScreen.kt, text size is 11.sp, change to 12.sp
    if "HomeScreen.kt" in file_path:
        text = text.replace("fontSize = 11.sp, //", "fontSize = 12.sp, //")
        text = text.replace("Spacer(modifier = Modifier.height(6.dp))", "Spacer(modifier = Modifier.height(8.dp))")
    
    # In PublishMenuBottomSheet.kt
    if "PublishMenuBottomSheet.kt" in file_path:
        text = text.replace("fontSize = 11.sp,", "fontSize = 12.sp,")
        text = text.replace("Spacer(Modifier.height(6.dp))", "Spacer(Modifier.height(8.dp))")

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(text)

modify_file("app/src/main/java/com/lianshan/lslife/feature/home/HomeScreen.kt")
modify_file("app/src/main/java/com/lianshan/lslife/ui/components/PublishMenuBottomSheet.kt")
