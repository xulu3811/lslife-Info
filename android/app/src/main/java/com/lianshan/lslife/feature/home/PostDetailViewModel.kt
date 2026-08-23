package com.lianshan.lslife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lianshan.lslife.core.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailState(
    val loading: Boolean = false,
    val post: Post? = null,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: LsRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    val currentUserId: String? = authRepo.cachedMe()?.id
    private val _state = MutableStateFlow(PostDetailState())
    val state = _state.asStateFlow()

    fun loadPost(id: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val res = repository.post(id)
            if (res.isSuccess) {
                _state.update { it.copy(loading = false, post = res.getOrNull()) }
            } else {
                _state.update { it.copy(loading = false, error = res.exceptionOrNull()?.message ?: "加载失败") }
            }
        }
    }



    fun toggleFavorite(onResult: (String) -> Unit) {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            val res = repository.toggleFavorite(post.id)
            if (res.isSuccess) {
                val data = res.getOrNull()
                if (data != null) {
                    _state.update {
                        it.copy(
                            post = it.post?.copy(
                                isFavorite = data.isFavorite,
                                likeCount = data.likeCount
                            )
                        )
                    }
                    onResult(if (data.isFavorite) "已收藏" else "已取消收藏")
                }
            } else {
                onResult(res.exceptionOrNull()?.message ?: "操作失败")
            }
        }
    }

    fun toggleFollow(userId: String) {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            val res = repository.toggleFollow(userId)
            if (res.isSuccess) {
                val isFollowing = res.getOrNull()?.get("isFollowing") == true
                _state.update {
                    it.copy(
                        post = it.post?.copy(
                            isFollowing = isFollowing
                        )
                    )
                }
            }
        }
    }

    fun auditAdminPost(action: String, note: String?, onResult: (String) -> Unit) {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            val res = repository.auditAdminPost(post.id, action, note)
            if (res.isSuccess) {
                onResult("审核操作成功")
            } else {
                onResult(res.exceptionOrNull()?.message ?: "审核操作失败")
            }
        }
    }
}
