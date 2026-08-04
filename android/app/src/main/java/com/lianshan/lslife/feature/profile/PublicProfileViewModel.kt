package com.lianshan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Post
import com.lianshan.lslife.core.network.PublicUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileState(
    val isLoading: Boolean = true,
    val user: PublicUserResponse? = null,
    val posts: List<Post> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PublicProfileState())
    val state: StateFlow<PublicProfileState> = _state.asStateFlow()

    fun loadProfileAndPosts(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // 1. 获取用户信息
            val userRes = repository.getUserPublicProfile(userId)
            if (userRes.isSuccess) {
                val user = userRes.getOrNull()
                _state.update { it.copy(user = user) }
            } else {
                _state.update { it.copy(isLoading = false, error = userRes.exceptionOrNull()?.message ?: "无法获取用户信息") }
                return@launch
            }
            
            // 2. 获取用户发布的帖子
            val postsRes = repository.posts(publisherId = userId, pageSize = 100)
            if (postsRes.isSuccess) {
                val postPage = postsRes.getOrNull()
                _state.update { it.copy(isLoading = false, posts = postPage?.list ?: emptyList()) }
            } else {
                _state.update { it.copy(isLoading = false, error = postsRes.exceptionOrNull()?.message ?: "无法获取用户的发布记录") }
            }
        }
    }
}
