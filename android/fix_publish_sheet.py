import sys
import re

file_path = "app/src/main/java/com/lianshan/lslife/ui/components/PublishMenuBottomSheet.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. ModalBottomSheet
sheet_search = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    )"""
sheet_replace = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF3F5F8),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFD1D5DB)) }
    )"""
text = text.replace(sheet_search, sheet_replace)

# 2. TabRow
tabrow_search = """            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFFFF4D4F),"""
tabrow_replace = """            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color(0xFFF3F5F8),
                contentColor = Color(0xFF4285F4),"""
text = text.replace(tabrow_search, tabrow_replace)

# 3. CommercePublishTab outer surface
com_outer_search = """        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F6F8),
            modifier = Modifier.fillMaxWidth()
        )"""
com_outer_replace = """        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )"""
text = text.replace(com_outer_search, com_outer_replace)

# 4. PublishMenuItemBox image
img_replace = """        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF0F4F9)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = item.iconUrl,
                    contentDescription = item.title,
                    modifier = Modifier.size(40.dp)
                )
            }
        }"""
text = re.sub(
    r"AsyncImage\(\s*model = item\.iconUrl,\s*contentDescription = item\.title,\s*modifier = Modifier\.size\(48\.dp\)[^\)]*\)",
    img_replace,
    text
)

# 5. MomentPublishTab outer surface
mom_outer_search = """        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF7F8FA),
            modifier = Modifier.fillMaxWidth()
        )"""
mom_outer_replace = """        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )"""
text = text.replace(mom_outer_search, mom_outer_replace)

# 6. AI Assist Pill colors
text = text.replace("color = Color(0xFFFFF0F0),", "color = Color(0xFFE8DEF8),")
text = text.replace("color = Color(0xFFFF4D4F), strokeWidth = 2.dp", "color = Color(0xFF1D192B), strokeWidth = 2.dp")
text = text.replace("color = Color(0xFFFF4D4F),", "color = Color(0xFF1D192B),") # Replaces AI icon/text color

# 7. Image Picker Add Icon
text = text.replace("tint = Color(0xFFFF4D4F),", "tint = Color(0xFF4285F4),")

# 8. Publish Button
text = text.replace("containerColor = Color(0xFFFF4D4F)", "containerColor = Color(0xFF4285F4)")

# Package name change for consistency just in case it is lianshan
text = text.replace("package com.lianshan.lslife", "package com.qingyuan.lslife")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)
