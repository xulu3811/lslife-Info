
import sys

file_path = "app/src/main/java/com/lianshan/lslife/feature/chat/ChatSessionListScreen.kt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

msg_search = """            } else if (session.lastMessage.startsWith("http") && (session.lastMessage.contains("chat_audio") || session.lastMessage.matches(Regex(".*\\\\.(mp3|m4a|wav|aac|ogg)(\\\\?.*)?", RegexOption.IGNORE_CASE)))) {
                "[语音]"
            } else {
                session.lastMessage
            }"""
            
msg_replace = """            } else if (session.lastMessage.startsWith("http") && (session.lastMessage.contains("chat_audio") || session.lastMessage.matches(Regex(".*\\\\.(mp3|m4a|wav|aac|ogg)(\\\\?.*)?", RegexOption.IGNORE_CASE)))) {
                "[语音]"
            } else if (session.lastMessage.trim().startsWith("{") && session.lastMessage.contains("\\"id\\"") && session.lastMessage.contains("\\"title\\"")) {
                "[商品/服务]"
            } else if (session.lastMessage.trim().startsWith("{") && session.lastMessage.contains("\\"lat\\"") && session.lastMessage.contains("\\"lng\\"")) {
                "[位置]"
            } else {
                session.lastMessage
            }"""

text = text.replace(msg_search, msg_replace)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(text)

