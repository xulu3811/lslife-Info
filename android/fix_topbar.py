
import sys

file_path = "app/src/main/java/com/lianshan/lslife/feature/chat/ChatSessionListScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

text = text.replace("val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()", "")
text = text.replace("modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),", "")

search = """            LargeTopAppBar(
                title = { 
                    Text(
                        text = "消息", 
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8),
                    scrolledContainerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8)
                ),
                scrollBehavior = scrollBehavior
            )"""

replace = """            TopAppBar(
                title = { 
                    Text(
                        text = "消息", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8),
                    scrolledContainerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8)
                )
            )"""

text = text.replace(search, replace)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

