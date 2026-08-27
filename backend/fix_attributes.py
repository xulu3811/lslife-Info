import os
import glob
import re

def fix_files():
    files = glob.glob('src/modules/*.ts')
    for file in files:
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()

        # Fix JSON.parse(p.attributes) -> (p.attributes as any)
        # Handle variations like JSON.parse(p.attributes || '{}')
        content = re.sub(r'JSON\.parse\((p|post|updatedPost)\.attributes(?:\s*\|\|\s*\'\{\}\')?\)', r'(\1.attributes as any)', content)
        
        # Fix JSON.stringify(body.attributes) -> body.attributes
        content = re.sub(r'JSON\.stringify\(body\.attributes\)', r'body.attributes', content)
        
        # Fix any raw JSON.parse(attributes) if it exists
        content = re.sub(r'JSON\.parse\(attributes\)', r'(attributes as any)', content)

        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)

    print("Fixed TS files!")

if __name__ == '__main__':
    fix_files()
