import re

with open('android/app/src/main/java/com/lianshan/lslife/feature/admin/AdminReportListScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

dummy_pattern = re.compile(r'@Composable\s*private fun ResourceGovernanceSection\(\)\s*\{.*?(?=@Composable\s*fun ReportTicketCard)', re.DOTALL)

new_impl = '''@Composable
private fun ResourceGovernanceSection(
    governanceViewModel: com.lianshan.lslife.feature.admin.GovernanceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by governanceViewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        com.lianshan.lslife.feature.admin.PostGovernanceTab(state, governanceViewModel, context)
        
        if (state.activeDialogAction != com.lianshan.lslife.feature.admin.GovernanceActionType.NONE) {
            com.lianshan.lslife.feature.admin.GovernanceDialog(state, governanceViewModel, context)
        }
    }
}

'''

content = dummy_pattern.sub(new_impl, content)

with open('android/app/src/main/java/com/lianshan/lslife/feature/admin/AdminReportListScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
