package com.qingyuan.lslife.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.model.AdminUserListItem
import com.qingyuan.lslife.core.network.AdminUserStatusRequest
import com.qingyuan.lslife.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUserListState(
    val loading: Boolean = false,
    val items: List<AdminUserListItem> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val keyword: String = ""
)

@HiltViewModel
class AdminUserListViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUserListState())
    val state = _state.asStateFlow()

    fun load(refresh: Boolean = false) {
        if (refresh) {
            _state.update { it.copy(page = 1, hasMore = true, items = emptyList()) }
        }
        val current = _state.value
        if (!current.hasMore || current.loading) return

        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val res = api.getAdminUsers(
                    page = current.page,
                    limit = 20,
                    keyword = current.keyword.takeIf { it.isNotBlank() }
                )
                if (res.code == 200 && res.data != null) {
                    val newItems = if (current.page == 1) res.data.items else current.items + res.data.items
                    _state.update {
                        it.copy(
                            loading = false,
                            items = newItems,
                            page = it.page + 1,
                            hasMore = newItems.size < res.data.total
                        )
                    }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "加载失败") }
            }
        }
    }

    fun search(keyword: String) {
        _state.update { it.copy(keyword = keyword) }
        load(refresh = true)
    }
    
    fun clearError() = _state.update { it.copy(error = null) }

    fun updateUserStatus(userId: String, status: String) {
        viewModelScope.launch {
            try {
                val res = api.updateAdminUserStatus(userId, AdminUserStatusRequest(status))
                if (res.code == 200) {
                    _state.update { st ->
                        st.copy(
                            items = st.items.map { if (it.id == userId) it.copy(status = status) else it }
                        )
                    }
                } else {
                    _state.update { it.copy(error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "更新失败") }
            }
        }
    }
}
