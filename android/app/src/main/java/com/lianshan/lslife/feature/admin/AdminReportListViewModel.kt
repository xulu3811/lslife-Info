package com.lianshan.lslife.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.model.Report
import com.lianshan.lslife.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminReportListState(
    val loading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val error: String? = null,
    val currentTab: String = "PENDING"
)

@HiltViewModel
class AdminReportListViewModel @Inject constructor(
    private val api: ApiService
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
                _state.update { it.copy(loading = false, error = e.message ?: "加载失败") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
