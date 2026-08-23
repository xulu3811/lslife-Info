import json

transcript_path = r'C:\Users\xl246\.gemini\antigravity-ide\brain\ed68c339-d578-4445-9515-015d29ec4850\.system_generated\logs\transcript_full.jsonl'

changes = []
with open(transcript_path, 'r', encoding='utf-8') as f:
    for line in f:
        if not line.strip(): continue
        try:
            obj = json.loads(line)
            if obj.get('type') == 'PLANNER_RESPONSE' and 'schema.prisma' in line:
                for tool in obj.get('tool_calls', []):
                    if tool.get('name') in ['multi_replace_file_content', 'replace_file_content']:
                        args = tool.get('args', {})
                        if 'schema.prisma' in args.get('TargetFile', ''):
                            chunks = args.get('ReplacementChunks', [])
                            if not chunks and 'ReplacementContent' in args:
                                chunks = [args]
                            for chunk in chunks:
                                changes.append(chunk.get('ReplacementContent', ''))
        except:
            pass

with open(r'D:\LsLife\schema_changes.txt', 'w', encoding='utf-8') as out:
    for i, c in enumerate(changes):
        out.write(f"--- CHANGE {i} ---\n{c}\n")
