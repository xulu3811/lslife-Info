import re
path = r'd:\GitHub-lslife-V6.0\android\app\src\main\res\values\strings.xml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()
text = re.sub(r'<string name="virtual_coin_name">.*?</string>', '<string name="virtual_coin_name">PC</string>', text)
with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
