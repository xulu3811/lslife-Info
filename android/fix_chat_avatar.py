
import sys
import re

file_path = "app/src/main/java/com/lianshan/lslife/feature/chat/ChatSessionListScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

avatar_search = """        // Avatar
        AsyncImage(
            model = session.peerAvatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )"""
avatar_replace = """        // Avatar
        com.qingyuan.lslife.ui.components.GoogleAvatar(
            url = session.peerAvatar,
            size = 48.dp
        )"""

text = text.replace(avatar_search, avatar_replace)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

