
import sys

file_path = "app/src/main/java/com/lianshan/lslife/feature/chat/ChatSessionListScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Fix package and imports if needed, it seems it checked out the com.lianshan package which is fine.
if "import androidx.compose.ui.input.nestedscroll.nestedScroll" not in text:
    text = text.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.input.nestedscroll.nestedScroll")

# Scaffold replacement
scaffold_search = """    Scaffold(
        topBar = {"""

scaffold_replace = """    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8),
        topBar = {"""
text = text.replace(scaffold_search, scaffold_replace)

# TopAppBar replacement
topbar_search = """            TopAppBar(
                title = { 
                    Text(
                        text = "消息", 
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )"""
topbar_replace = """            LargeTopAppBar(
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
text = text.replace(topbar_search, topbar_replace)

# LazyColumn replacement
lazy_search = """            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {"""
lazy_replace = """            androidx.compose.material3.Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, bottom = 16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = androidx.compose.ui.graphics.Color.White,
                shadowElevation = 0.dp
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {"""
text = text.replace(lazy_search, lazy_replace)

# To close the Surface, we just look for where LazyColumn ends before the BottomSheet.
lazy_end_search = """                    )
                }
            }
        }
    }

    if (selectedSessionForAction != null) {"""
lazy_end_replace = """                    )
                }
            }
            } // End of Surface
        }
    }

    if (selectedSessionForAction != null) {"""
text = text.replace(lazy_end_search, lazy_end_replace)

# Item background
item_bg_search = ".background(if (isPinned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background)"
item_bg_replace = ".background(if (isPinned) androidx.compose.ui.graphics.Color(0xFFF8F9FA) else androidx.compose.ui.graphics.Color.Transparent)"
text = text.replace(item_bg_search, item_bg_replace)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

