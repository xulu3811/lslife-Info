import re

with open('src/components/UserProfile.tsx', 'r') as f:
    content = f.read()

state_decl = "  const [showSettings, setShowSettings] = useState<boolean>(false);"
new_state_decl = """  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [activeSettingView, setActiveSettingView] = useState<'main' | 'profile' | 'notifications' | 'privacy' | 'locations' | 'terms' | 'policy'>('main');
"""

content = content.replace(state_decl, new_state_decl)

with open('src/components/UserProfile.tsx', 'w') as f:
    f.write(content)

