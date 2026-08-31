package com.qingyuan.lslife.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.model.ServerMonitorData
import com.qingyuan.lslife.core.network.AdminApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerMonitorViewModel @Inject constructor(
    private val api: AdminApiService
) : ViewModel() {

    private val _serverState = MutableStateFlow<ServerMonitorData?>(null)
    val serverState: StateFlow<ServerMonitorData?> = _serverState.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val res = api.getServerStatus()
                    _serverState.value = res.data
                    _loading.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000) // 3 seconds polling
            }
        }
    }
}
