import re

with open('backend/src/modules/dynamics.ts', 'r', encoding='utf-8') as f:
    content = f.read()

replacement = '''const moderationResult = moderateContent(body.category || "", body.content);
      
      if (moderationResult.matchedWords && moderationResult.matchedWords.length > 0) {
        await prisma.moderationLog.create({
          data: {
            userId,
            action: 'PUBLISH_DYNAMIC',
            content: (body.category || "") + " " + body.content,
            matchedWords: moderationResult.matchedWords.join(','),
            level: moderationResult.level || 0,
            result: moderationResult.status
          }
        });
      }

      if (moderationResult.pass) {'''

content = re.sub(r'const moderationResult = await moderateContent\(body\.category, body\.content\);\s*if \(moderationResult\.pass\) \{', replacement, content)

with open('backend/src/modules/dynamics.ts', 'w', encoding='utf-8') as f:
    f.write(content)
