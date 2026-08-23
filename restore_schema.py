import json

transcript_path = r'C:\Users\xl246\.gemini\antigravity-ide\brain\ed68c339-d578-4445-9515-015d29ec4850\.system_generated\logs\transcript_full.jsonl'

with open(transcript_path, 'r', encoding='utf-8') as f:
    for line in f:
        if not line.strip(): continue
        try:
            obj = json.loads(line)
            if obj.get('type') == 'TOOL_RESPONSE' and 'model PersonIdentity {' in obj.get('content', ''):
                content = obj['content']
                if len(content) > 10000:
                    lines = content.split('\n')
                    actual_lines = []
                    for l in lines:
                        if ':' in l and l.split(':')[0].isdigit():
                            actual_lines.append(l.split(':', 1)[1].lstrip(' '))
                    
                    with open(r'D:\LsLife\backend\prisma\schema.prisma', 'w', encoding='utf-8') as out:
                        out.write('\n'.join(actual_lines))
                    print('Restored successfully!')
                    break
        except Exception as e:
            pass
