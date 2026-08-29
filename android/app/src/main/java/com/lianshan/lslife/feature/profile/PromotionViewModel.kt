package com.qingyuan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.network.PromotionBuyRequest
import com.qingyuan.lslife.core.network.PromotionStatsResponse
import com.qingyuan.lslife.core.network.PromotionTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.qingyuan.lslife.core.model.Post

data class PromotionState(
    val isLoading: Boolean = false,
    val stats: PromotionStatsResponse? = null,
    val myTasks: List<PromotionTask> = emptyList(),
    val myPosts: List<Post> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PromotionViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PromotionState())
    val state: StateFlow<PromotionState> = _state.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val statsResult = repository.getPromotionStats()
            val tasksResult = repository.getMyPromotions()
            // 随便获取首页前 20 个帖子，此处简单模拟获取自己的帖子
            val postsResult = repository.getMyPostsForPromotion()

            _state.update { 
                it.copy(
                    isLoading = false,
                    stats = statsResult.getOrNull(),
                    myTasks = tasksResult.getOrNull() ?: emptyList(),
                    myPosts = postsResult.getOrNull()?.list ?: emptyList(),
                    error = statsResult.exceptionOrNull()?.message ?: tasksResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun buyPromotion(postId: String, type: String, days: Int? = 1, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val req = PromotionBuyRequest(postId = postId, type = type, days = days)
            val result = repository.buyPromotion(req)
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                fetchData() // 刷新数据
                onSuccess()
            }.onFailure {
                onError(it.message ?: "购买失败")
            }
        }
    }

    fun rechargeCards(quantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val req = com.qingyuan.lslife.core.network.RechargeCardsRequest(quantity = quantity)
            val result = repository.rechargeCards(req)
            _state.update { it.copy(isLoading = false) }

            result.onSuccess {
                fetchData()
                onSuccess()
            }.onFailure {
                onError(it.message ?: "充值失败")
            }
        }
    }
}
