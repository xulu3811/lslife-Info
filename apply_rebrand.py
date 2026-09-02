import os
import re

# 1. Update AGENTS.md
def update_agents_md(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace V8.36 -> V1.0
    content = re.sub(r'[Vv]8\.36', 'V1.0', content)
    # Replace Full Name
    content = content.replace("同城清远 (Qingyuan Smart Local Life Service Platform)", "清远智慧同城生活服务平台项目")
    # Replace other '同城清远' with '清远同城'
    content = content.replace("同城清远", "清远同城")
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

update_agents_md("AGENTS.md")
update_agents_md(".agents/AGENTS.md")

# 2. Update Android build.gradle.kts
build_gradle_path = "android/app/build.gradle.kts"
if os.path.exists(build_gradle_path):
    with open(build_gradle_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    content = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 1', content)
    content = re.sub(r'versionName\s*=\s*".*?"', 'versionName = "1.0"', content)
    
    with open(build_gradle_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 3. Global replacement of "同城清远" to "清远同城" in source files
# We will do a full walk for source code files
def walk_and_replace():
    for root, dirs, files in os.walk("."):
        if ".git" in root or "node_modules" in root or "build" in root or ".gradle" in root:
            continue
        for file in files:
            if file.endswith((".kt", ".xml", ".ts", ".tsx", ".json", ".md")):
                filepath = os.path.join(root, file)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    if "同城清远" in content:
                        new_content = content.replace("同城清远", "清远同城")
                        
                        # Apply special fix for full project name in strings.xml or metadata if needed?
                        # The abbreviation is fine for everywhere, except maybe app_name where it's better to be the full name or abbreviation. The user says "简称 清远同城", so the app name is probably "清远同城".
                        
                        with open(filepath, 'w', encoding='utf-8') as f:
                            f.write(new_content)
                except Exception:
                    pass

walk_and_replace()
print("All replacements done.")
