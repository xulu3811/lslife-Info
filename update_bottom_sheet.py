import re

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\components\PublishMenuBottomSheet.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove MomentScenario and MomentPublishTab
content = re.sub(r'data class MomentScenario.*?(?=@Composable\s+private fun RowScope\.PublishMenuItemBox)', '', content, flags=re.DOTALL)

# Replace the whole ModalBottomSheet content
new_bottom_sheet = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishMenuBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToPublish: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommercePublishTab(onDismiss, onNavigateToPublish)
        }
    }
}
"""

content = re.sub(r'@OptIn\(ExperimentalMaterial3Api::class\)\s+@Composable\s+fun PublishMenuBottomSheet.*?\}\s*\}\s*\}', new_bottom_sheet, content, flags=re.DOTALL)

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\ui\components\PublishMenuBottomSheet.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated PublishMenuBottomSheet.kt")
