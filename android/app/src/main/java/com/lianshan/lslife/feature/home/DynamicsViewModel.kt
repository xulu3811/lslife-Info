package com.lianshan.lslife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DynamicsUiState {
    object Loading : DynamicsUiState()
    data class Success(val items: List<Post>, val hasMore: Boolean = false) : DynamicsUiState()
    data class Error(val message: String) : DynamicsUiState()
}

@HiltViewModel
class DynamicsViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DynamicsUiState>(DynamicsUiState.Loading)
    val uiState: StateFlow<DynamicsUiState> = _uiState.asStateFlow()

    private val _newPostsCount = MutableStateFlow(0)
    val newPostsCount: StateFlow<Int> = _newPostsCount.asStateFlow()
    
    private var currentPage = 1
    private val currentPosts = mutableListOf<Post>()
    private var isLoadingMore = false

    init {
        loadInitialData()
        setupWebSocket()
    }

    fun loadInitialData() {
        _uiState.value = DynamicsUiState.Loading
        currentPage = 1
        fetchData(page = currentPage)
    }
    
    fun loadMore() {
        if (isLoadingMore) return
        if (_uiState.value is DynamicsUiState.Success && (_uiState.value as DynamicsUiState.Success).hasMore) {
            isLoadingMore = true
            currentPage++
            fetchData(page = currentPage)
        }
    }

    private fun fetchData(page: Int) {
        viewModelScope.launch {
            // TODO: In a real environment, obtain actual GPS coordinates via LocationManager/AMap SDK
            // For now, using simulated LBS (e.g. Guangzhou Tower)
            val currentLat = 23.1291
            val currentLng = 113.2644

            repository.posts(category = "sys_dynamic", postType = "MOMENT", page = page, pageSize = 20)
                .onSuccess { resPage ->
                    if (page == 1) {
                        currentPosts.clear()
                    }
                    currentPosts.addAll(resPage.list)
                    _uiState.value = DynamicsUiState.Success(
                        items = currentPosts.toList(),
                        hasMore = resPage.page * resPage.pageSize < resPage.total
                    )
                }
                .onFailure {
                    if (page == 1) {
                        _uiState.value = DynamicsUiState.Error(it.message ?: "Failed to load dynamics")
                    }
                }
            isLoadingMore = false
        }
    }

    private fun setupWebSocket() {
        // In a real app, inject a WebSocketService and collect events
        // Simulated WebSocket push for NEW_DYNAMIC
        viewModelScope.launch {
            // delay(10000)
            // _newPostsCount.update { it + 3 }
        }
    }

    fun fetchNewData() {
        _newPostsCount.value = 0
        loadInitialData()
    }
}
