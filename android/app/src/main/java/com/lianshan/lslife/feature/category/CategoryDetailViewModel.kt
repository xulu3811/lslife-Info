package com.qingyuan.lslife.feature.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.CategoryRepository
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.model.CategoryNode
import com.qingyuan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryDetailState(
    val category: CategoryNode? = null,
    val subCategories: List<CategoryNode> = emptyList(),
    val selectedSubCategory: String = "all",
    val posts: List<Post> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""

    private val _state = MutableStateFlow(CategoryDetailState())
    val state: StateFlow<CategoryDetailState> = _state

    init {
        loadCategoryInfo()
        loadPosts(showFullLoading = true, page = 1)
    }

    private fun loadCategoryInfo() {
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                val cat = tree.find { it.id == categoryId }
                val subs = cat?.children ?: emptyList()
                _state.update { it.copy(category = cat, subCategories = subs) }
            }
        }
        viewModelScope.launch { categoryRepo.getCategoryTree() }
    }

    fun onSubCategory(subId: String) {
        if (_state.value.selectedSubCategory == subId) return
        _state.update { it.copy(selectedSubCategory = subId) }
        loadPosts(showFullLoading = true, page = 1)
    }

    fun refresh() {
        loadPosts(isRefresh = true, page = 1)
    }

    fun loadMore() {
        if (!_state.value.hasMore || _state.value.loading || _state.value.loadingMore) return
        loadPosts(page = _state.value.page + 1)
    }

    private fun loadPosts(showFullLoading: Boolean = false, isRefresh: Boolean = false, page: Int = 1) {
        viewModelScope.launch {
            if (showFullLoading) _state.update { it.copy(loading = true, error = null) }
            if (isRefresh) _state.update { it.copy(refreshing = true, error = null) }
            if (page > 1) _state.update { it.copy(loadingMore = true, error = null) }

            val targetCategory = if (_state.value.selectedSubCategory == "all") categoryId else _state.value.selectedSubCategory
            val result = repo.posts(category = targetCategory, page = page, pageSize = 20)

            result.onSuccess { postPage ->
                val fetchedPosts = postPage.list
                _state.update {
                    it.copy(
                        posts = if (page == 1) fetchedPosts else it.posts + fetchedPosts,
                        page = page,
                        hasMore = fetchedPosts.size == 20,
                        loading = false,
                        refreshing = false,
                        loadingMore = false
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
