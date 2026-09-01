import os
import re

files = [
    r"d:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\wallet\WalletScreen.kt",
    r"d:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\profile\MembershipScreen.kt",
    r"d:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\profile\PromotionCenterScreen.kt"
]

for file in files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "WalletScreen" in file:
        content = re.sub(r"// Google Primary Colors\n.*?\n.*?\n.*?\n.*?\n\n", "", content)
        content = re.sub(r"// Google Tonal Backgrounds\n.*?\n.*?\n.*?\n.*?\n\n", "", content)
        content = re.sub(r"private val GoogleGreyBorder =.*?\n", "", content)
        content = re.sub(r"private val GoogleTextPrimary =.*?\n", "", content)
        content = re.sub(r"private val GoogleTextSecondary =.*?\n", "", content)
        
        replacements = {
            "GoogleBlueLight": "MaterialTheme.colorScheme.primaryContainer",
            "GoogleBlue": "MaterialTheme.colorScheme.primary",
            "GoogleRedLight": "MaterialTheme.colorScheme.errorContainer",
            "GoogleRed": "MaterialTheme.colorScheme.error",
            "GoogleYellowLight": "MaterialTheme.colorScheme.tertiaryContainer",
            "GoogleYellow": "MaterialTheme.colorScheme.tertiary",
            "GoogleGreenLight": "MaterialTheme.colorScheme.secondaryContainer",
            "GoogleGreen": "MaterialTheme.colorScheme.secondary",
            "GoogleGreyBorder": "MaterialTheme.colorScheme.outlineVariant",
            "GoogleTextPrimary": "MaterialTheme.colorScheme.onSurface",
            "GoogleTextSecondary": "MaterialTheme.colorScheme.onSurfaceVariant",
        }
        for k, v in replacements.items():
            content = content.replace(k, v)
            
        # specifically fix the red tag in WalletScreen
        content = content.replace(".background(MaterialTheme.colorScheme.error)", ".background(MaterialTheme.colorScheme.tertiaryContainer)")
        content = content.replace("color = Color.White", "color = MaterialTheme.colorScheme.onTertiaryContainer")
            
    elif "MembershipScreen" in file or "PromotionCenterScreen" in file:
        content = content.replace("import com.qingyuan.lslife.ui.theme.PrimaryRed\n", "")
        content = content.replace("PrimaryRed", "MaterialTheme.colorScheme.primary")
        content = content.replace("Color(0xFFE2E8F0)", "MaterialTheme.colorScheme.outlineVariant")
        content = content.replace("Color(0xFF1E293B)", "MaterialTheme.colorScheme.onSurface")
        content = content.replace("Color(0xFF94A3B8)", "MaterialTheme.colorScheme.onSurfaceVariant")
        content = content.replace("Color(0xFFCBD5E1)", "MaterialTheme.colorScheme.outline")
        
        # fix red tags
        content = content.replace(".background(MaterialTheme.colorScheme.primary)", ".background(MaterialTheme.colorScheme.tertiaryContainer)")
        content = content.replace("color = Color.White", "color = MaterialTheme.colorScheme.onTertiaryContainer")

    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)
