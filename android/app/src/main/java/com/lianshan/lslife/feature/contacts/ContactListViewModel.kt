package com.lianshan.lslife.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.PublicUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactListState(
    val loading: Boolean = false,
    val friends: List<PublicUserResponse> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ContactListState())
    val state: StateFlow<ContactListState> = _state

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = api.getFriendList()
                if (res.code == 0 && res.data != null) {
                    _state.update { it.copy(loading = false, friends = res.data.friends) }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.localizedMessage ?: "加载联系人失败") }
            }
        }
    }
}
