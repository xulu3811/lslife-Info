import re

with open('android/app/src/main/java/com/lianshan/lslife/feature/admin/AdminReportListViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'import com.lianshan.lslife.core.network.ImportSensitiveWordsRequest',
    'import com.lianshan.lslife.core.network.ImportSensitiveWordsRequest\nimport com.lianshan.lslife.core.network.ModerationLog'
)

content = content.replace(
    'val toastMessage: String? = null',
    'val toastMessage: String? = null,\n    val logs: List<ModerationLog> = emptyList(),\n    val logsLoading: Boolean = false'
)

new_func = '''    fun loadModerationLogs() {
        viewModelScope.launch {
            _state.update { it.copy(logsLoading = true) }
            try {
                val res = api.getModerationLogs(page = 1, pageSize = 100)
                if (res.code == 200 && res.data != null) {
                    _state.update { it.copy(logs = res.data.list, logsLoading = false) }
                } else {
                    _state.update { it.copy(logsLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(logsLoading = false) }
            }
        }
    }'''

content = content.replace('fun loadWords() {', new_func + '\n\n    fun loadWords() {')

with open('android/app/src/main/java/com/lianshan/lslife/feature/admin/AdminReportListViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
