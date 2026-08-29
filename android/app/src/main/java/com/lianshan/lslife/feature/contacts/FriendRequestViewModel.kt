package com.qingyuan.lslife.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.network.ApiService
import com.qingyuan.lslife.core.network.FriendHandleRequest
import com.qingyuan.lslife.core.network.FriendRequestItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendRequestState(
    val loading: Boolean = false,
    val requests: List<FriendRequestItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(FriendRequestState())
    val state: StateFlow<FriendRequestState> = _state

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = api.getFriendRequests()
                if (res.code == 0 && res.data != null) {
                    _state.update { it.copy(loading = false, requests = res.data.requests) }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.localizedMessage ?: "加载失败") }
            }
        }
    }

    fun handleRequest(requestId: String, action: String) {
        viewModelScope.launch {
            try {
                val res = api.handleFriendRequest(FriendHandleRequest(requestId, action))
                if (res.code == 0) {
                    // Remove from list
                    val newList = _state.value.requests.filter { it.id != requestId }
                    _state.update { it.copy(requests = newList) }
                } else {
                    _state.update { it.copy(error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage) }
            }
        }
    }
}
