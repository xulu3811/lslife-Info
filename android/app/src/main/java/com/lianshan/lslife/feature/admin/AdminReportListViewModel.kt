package com.lianshan.lslife.feature.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.model.Report
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.SensitiveWord
import com.lianshan.lslife.core.network.SensitiveWordRequest
import com.lianshan.lslife.core.network.ImportSensitiveWordsRequest
import com.lianshan.lslife.core.network.ModerationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

data class AdminReportListState(
    val loading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val error: String? = null,
    val currentTab: String = "PENDING",
    val words: List<SensitiveWord> = emptyList(),
    val wordsLoading: Boolean = false,
    val toastMessage: String? = null,
    val logs: List<ModerationLog> = emptyList(),
    val logsLoading: Boolean = false
)

@HiltViewModel
class AdminReportListViewModel @Inject constructor(
    val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(AdminReportListState())
    val state = _state.asStateFlow()

    fun load(status: String = _state.value.currentTab) {
        _state.update { it.copy(loading = true, error = null, currentTab = status) }
        viewModelScope.launch {
            try {
                val res = api.getAdminReports(status)
                if (res.code == 200) {
                    _state.update { it.copy(loading = false, reports = res.data ?: emptyList()) }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }

        fun loadModerationLogs() {
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
    }

    fun loadWords() {
        _state.update { it.copy(wordsLoading = true) }
        viewModelScope.launch {
            try {
                val res = api.getSensitiveWords(1, 100)
                if (res.code == 200) {
                    _state.update { it.copy(wordsLoading = false, words = res.data?.list ?: emptyList()) }
                } else {
                    _state.update { it.copy(wordsLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(wordsLoading = false) }
            }
        }
    }

    fun addWord(word: String, level: Int) {
        viewModelScope.launch {
            try {
                val res = api.addSensitiveWord(SensitiveWordRequest(word = word, level = level))
                if (res.code == 200) {
                    _state.update { it.copy(toastMessage = "添加成功") }
                    loadWords()
                } else {
                    _state.update { it.copy(toastMessage = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(toastMessage = "添加失败，词汇可能已存在") }
            }
        }
    }
    
    fun importWordsFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(toastMessage = "正在解析并导入词库...") }
                val lines = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    reader.readLines()
                }
                
                val requests = lines.map { it.trim() }.filter { it.isNotEmpty() }.map { 
                    // Assume default level 3 for imported blocked words
                    SensitiveWordRequest(word = it, level = 3)
                }
                
                if (requests.isEmpty()) {
                    _state.update { it.copy(toastMessage = "TXT文件内容为空") }
                    return@launch
                }
                
                val res = api.importSensitiveWords(ImportSensitiveWordsRequest(words = requests))
                if (res.code == 200) {
                    _state.update { it.copy(toastMessage = "成功导入 " + res.data?.added + " 个风控词汇") }
                    loadWords()
                } else {
                    _state.update { it.copy(toastMessage = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(toastMessage = "导入失败: " + e.message) }
            }
        }
    }
    
    fun deleteWord(id: String) {
        viewModelScope.launch {
            try {
                val res = api.deleteSensitiveWord(id)
                if (res.code == 200) {
                    _state.update { it.copy(toastMessage = "删除成功") }
                    loadWords()
                }
            } catch (e: Exception) {}
        }
    }
    
    fun clearToast() = _state.update { it.copy(toastMessage = null) }
    fun clearError() = _state.update { it.copy(error = null) }
}
