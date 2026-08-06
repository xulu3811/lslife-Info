package com.lianshan.lslife.feature.home

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

data class PostDetailState(
    val loading: Boolean = false,
    val post: Post? = null,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {
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

    fun addToCart(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val post = _state.value.post ?: return
        if (post.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH || post.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO) {
            onError("信息类服务不支持加入购物车，请直接联系发布者")
            return
        }
        viewModelScope.launch {
            repository.upsertCart(postId = post.id, quantity = 1)
            onSuccess()
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
}
