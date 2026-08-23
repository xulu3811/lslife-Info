package com.lianshan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Post
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
    private val repository: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MyPostsState())
    val state = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            val res = repository.posts(mine = true, pageSize = 50) // load up to 50 for now
            if (res.isSuccess) {
                _state.update { it.copy(loading = false, posts = res.getOrNull()?.list.orEmpty()) }
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
