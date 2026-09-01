import re
import os

files = {
    'wallet': r'd:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\wallet\WalletScreen.kt',
    'membership': r'd:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\profile\MembershipScreen.kt',
    'promo': r'd:\GitHub-lslife-V6.0\android\app\src\main\java\com\lianshan\lslife\feature\profile\PromotionCenterScreen.kt'
}

for key, file_path in files.items():
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    if key == 'wallet':
        content = re.sub(r'// Google Primary Colors\n.*?\n.*?\n.*?\n.*?\n\n', '', content)
        content = re.sub(r'// Google Tonal Backgrounds\n.*?\n.*?\n.*?\n.*?\n\n', '', content)
        content = re.sub(r'private val GoogleGreyBorder =.*?\n', '', content)
        content = re.sub(r'private val GoogleTextPrimary =.*?\n', '', content)
        content = re.sub(r'private val GoogleTextSecondary =.*?\n', '', content)

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
        
        # specific fixes for badge and buttons
        content = content.replace('.background(MaterialTheme.colorScheme.error)\n                                        .padding(horizontal = 4.dp, vertical = 2.dp)', 
                                  '.background(MaterialTheme.colorScheme.tertiaryContainer)\n                                        .padding(horizontal = 4.dp, vertical = 2.dp)')
        content = content.replace('color = Color.White\n                                    )', 'color = MaterialTheme.colorScheme.onTertiaryContainer\n                                    )')
        
        # Button text
        content = content.replace('color = Color.White\n                            )', 'color = MaterialTheme.colorScheme.onPrimary\n                            )')
        
    else:
        content = content.replace('import com.qingyuan.lslife.ui.theme.PrimaryRed\n', '')
        content = content.replace('PrimaryRed', 'MaterialTheme.colorScheme.primary')
        content = content.replace('Color(0xFFE2E8F0)', 'MaterialTheme.colorScheme.outlineVariant')
        content = content.replace('Color(0xFF1E293B)', 'MaterialTheme.colorScheme.onSurface')
        content = content.replace('Color(0xFF94A3B8)', 'MaterialTheme.colorScheme.onSurfaceVariant')
        content = content.replace('Color(0xFFCBD5E1)', 'MaterialTheme.colorScheme.outline')
        
        # fix badge background
        content = content.replace('.background(MaterialTheme.colorScheme.primary)\n                          .padding(horizontal = 6.dp, vertical = 2.dp)', 
                                  '.background(MaterialTheme.colorScheme.tertiaryContainer)\n                          .padding(horizontal = 6.dp, vertical = 2.dp)')
        content = content.replace('.background(MaterialTheme.colorScheme.primary)\n                          .padding(horizontal = 4.dp, vertical = 1.dp)',
                                  '.background(MaterialTheme.colorScheme.tertiaryContainer)\n                          .padding(horizontal = 4.dp, vertical = 1.dp)')
                                  
        # fix badge text color
        content = content.replace('color = Color.White,\n                          fontWeight = FontWeight.Medium',
                                  'color = MaterialTheme.colorScheme.onTertiaryContainer,\n                          fontWeight = FontWeight.Medium')
                                  
        # fix button text color
        content = content.replace('color = Color.White\n                        )', 'color = MaterialTheme.colorScheme.onPrimary\n                        )')
        content = content.replace('color = Color.White\n                              )', 'color = MaterialTheme.colorScheme.onPrimary\n                              )')
        
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Done")
