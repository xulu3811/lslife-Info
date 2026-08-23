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

data class FootprintsState(
    val loading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class FootprintsViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FootprintsState())
    val state = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            val res = repository.getFootprints(page = 1, pageSize = 50)
            if (res.isSuccess) {
                _state.update { it.copy(loading = false, posts = res.getOrNull()?.list.orEmpty()) }
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "加载失败") }
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            val res = repository.clearFootprints()
            if (res.isSuccess) {
                _state.update { it.copy(posts = emptyList(), message = "已清空") }
            } else {
                _state.update { it.copy(message = res.exceptionOrNull()?.message ?: "清空失败") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
