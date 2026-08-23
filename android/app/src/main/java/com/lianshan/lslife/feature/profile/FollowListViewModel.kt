package com.lianshan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.network.FollowUserItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowListState(
    val loading: Boolean = false,
    val tabIndex: Int = 0,
    val followingList: List<FollowUserItem> = emptyList(),
    val followersList: List<FollowUserItem> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class FollowListViewModel @Inject constructor(
    private val repository: LsRepository,
    private val authRepository: com.lianshan.lslife.core.data.AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FollowListState())
    val state = _state.asStateFlow()

    private var userId: String = ""

    fun initialize(id: String) {
        val finalId = if (id.isEmpty()) authRepository.cachedMe()?.id ?: return else id
        if (userId == finalId) return
        userId = finalId
        loadFollowing()
        loadFollowers()
    }

    fun setTab(index: Int) {
        _state.update { it.copy(tabIndex = index) }
    }

    private fun loadFollowing() {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val res = repository.getFollowing(userId, 1, 50)
            if (res.isSuccess) {
                _state.update { it.copy(loading = false, followingList = res.getOrNull()?.list.orEmpty()) }
            } else {
                _state.update { it.copy(loading = false, message = res.exceptionOrNull()?.message ?: "????") }
            }
        }
    }

    private fun loadFollowers() {
        viewModelScope.launch {
            val res = repository.getFollowers(userId, 1, 50)
            if (res.isSuccess) {
                _state.update { it.copy(followersList = res.getOrNull()?.list.orEmpty()) }
            } else {
                _state.update { it.copy(message = res.exceptionOrNull()?.message ?: "????") }
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            val res = repository.toggleFollow(targetUserId)
            if (res.isSuccess) {
                loadFollowing()
                loadFollowers()
                _state.update { it.copy(message = "????") }
            } else {
                _state.update { it.copy(message = res.exceptionOrNull()?.message ?: "????") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
