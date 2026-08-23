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

data class FavoritesState(
    val loading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FavoritesState())
    val state = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            val res = repository.getFavorites(page = 1, pageSize = 50)
            if (res.isSuccess) {
                _state.update { it.copy(loading = false, posts = res.getOrNull()?.list.orEmpty()) }
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "加载收藏失败") }
            }
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            val res = repository.toggleFavorite(id)
            if (res.isSuccess) {
                _state.update { s ->
                    s.copy(
                        posts = s.posts.filter { it.id != id },
                        message = "已取消收藏"
                    )
                }
            } else {
                _state.update { it.copy(message = res.exceptionOrNull()?.message ?: "操作失败") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
