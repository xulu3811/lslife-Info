const fs = require('fs');
const path = require('path');

const transcriptPath = path.join('C:\\Users\\xl246\\.gemini\\antigravity-ide\\brain\\ed68c339-d578-4445-9515-015d29ec4850\\.system_generated\\logs\\transcript_full.jsonl');
const lines = fs.readFileSync(transcriptPath, 'utf8').split('\n');

for (const line of lines) {
  if (!line.trim()) continue;
  try {
    const obj = JSON.parse(line);
    if (obj.tool_calls) {
       for (const tool of obj.tool_calls) {
          if (tool.function.name === 'default_api:view_file') {
             console.log('Call: ' + tool.function.arguments);
          }
       }
    }
    if (obj.type === 'TOOL_RESPONSE') {
       if (obj.content.includes('schema.prisma')) {
          console.log('Response with schema.prisma length: ' + obj.content.length);
          if (obj.content.length > 10000) {
             const linesArr = obj.content.split('\n');
             const actualLines = [];
             for (const l of linesArr) {
                if (l.match(/^\d+:/)) {
                   actualLines.push(l.replace(/^\d+:\s?/, ''));
                }
             }
             fs.writeFileSync('D:\\LsLife\\backend\\prisma\\schema.prisma.restored', actualLines.join('\n'));
             console.log('Saved schema.prisma.restored');
          }
       }
    }
  } catch(e) {}
}
