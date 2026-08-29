package com.qingyuan.lslife.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.core.model.PostUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminReviewListState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminReviewListViewModel @Inject constructor(
    private val api: com.qingyuan.lslife.core.network.ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(AdminReviewListState())
    val state: StateFlow<AdminReviewListState> = _state.asStateFlow()

    init {
        loadReviewList()
    }

    fun loadReviewList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val res = api.getAdminPosts("MANUAL_REVIEWING")
                if (res.code == 0) {
                    _state.update { it.copy(posts = res.data ?: emptyList(), isLoading = false) }
                } else {
                    _state.update { it.copy(error = res.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "加载失败", isLoading = false) }
            }
        }
    }
}
