import re

with open('backend/src/modules/publish.ts', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'userId: user\.id,\s*action: \'PUBLISH_POST\'', r'userId: req.userId || user.id,\n          action: \'PUBLISH_POST\'', content)

with open('backend/src/modules/publish.ts', 'w', encoding='utf-8') as f:
    f.write(content)
