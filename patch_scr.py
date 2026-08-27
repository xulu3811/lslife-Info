import re

with open('android/app/src/main/java/com/lianshan/lslife/feature/admin/AdminReportListScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('listOf("举报处理", "资源治理", "风控词库")', 'listOf("举报处理", "资源治理", "风控词库", "拦截记录")')

new_tab = '''            } else if (topLevelTab == 3) {
                ModerationLogsSection(state = state, viewModel = viewModel)
            }'''

content = content.replace('            } else if (topLevelTab == 2) {\n                RiskDictionarySection(state = state, viewModel = viewModel)\n            }', '            } else if (topLevelTab == 2) {\n                RiskDictionarySection(state = state, viewModel = viewModel)\n' + new_tab)

with open('android/app/src/main/java/com/lianshan/lslife/feature/admin/AdminReportListScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
