package com.qingyuan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPostsState(
    val loading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val repository: LsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val filterStatus = savedStateHandle.get<String>("status") ?: "ALL"
    private val _state = MutableStateFlow(MyPostsState())
    val state = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            val res = repository.posts(mine = true, pageSize = 50) // load up to 50 for now
            if (res.isSuccess) {
                var list = res.getOrNull()?.list.orEmpty()
                if (filterStatus != "ALL") {
                    list = list.filter { post ->
                        val ps = post.status.uppercase()
                        when (filterStatus) {
                            "PENDING" -> ps == "PENDING_REVIEW" || ps == "AI_REVIEWING" || ps == "MANUAL_REVIEWING"
                            "PUBLISHED" -> ps == "PUBLISHED"
                            "ARCHIVED" -> ps == "REMOVED"
                            else -> true
                        }
                    }
                }
                _state.update { it.copy(loading = false, posts = list) }
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "加载失败") }
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val res = repository.updatePostStatus(id, status)
            if (res.isSuccess) {
                _state.update { it.copy(message = "操作成功") }
                load()
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "操作失败") }
            }
        }
    }

    fun refreshPost(id: String) {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val res = repository.refreshPost(id)
            if (res.isSuccess) {
                _state.update { it.copy(message = "擦亮成功！") }
                load()
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "擦亮失败") }
            }
        }
    }

    fun deletePost(id: String) {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val res = repository.deletePost(id)
            if (res.isSuccess) {
                _state.update { it.copy(message = "删除成功") }
                load()
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "删除失败") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
