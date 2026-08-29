package com.qingyuan.lslife.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.network.ApiService
import com.qingyuan.lslife.core.network.FriendRequestPayload
import com.qingyuan.lslife.core.network.PublicUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddFriendState(
    val loading: Boolean = false,
    val searchResults: List<PublicUserResponse> = emptyList(),
    val error: String? = null,
    val successMsg: String? = null
)

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(AddFriendState())
    val state: StateFlow<AddFriendState> = _state

    fun searchUser(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, successMsg = null) }
            try {
                val res = api.searchUsers(query)
                if (res.code == 0 && res.data != null) {
                    _state.update { it.copy(loading = false, searchResults = res.data) }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.localizedMessage) }
            }
        }
    }

    fun sendRequest(userId: String, message: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, successMsg = null) }
            try {
                val res = api.sendFriendRequest(FriendRequestPayload(userId, message))
                if (res.code == 0) {
                    _state.update { it.copy(loading = false, successMsg = "好友请求已发送") }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.localizedMessage) }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(error = null, successMsg = null) }
    }
}
