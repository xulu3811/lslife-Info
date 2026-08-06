package com.lianshan.lslife.feature.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.CategoryRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val topCategories: List<CategoryNode> = emptyList(),
    val selectedTabIndex: Int = 0,
    val parentCategoryId: String = "",
    val categoryTree: List<CategoryNode> = emptyList(),
    val subCategories: List<CategoryNode> = emptyList(),
    val selectedSubCategory: String = "all",
    val posts: List<Post> = emptyList(),
    val sort: String = "default",
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state

    init {
        observeCategoryTree()
    }

    private fun observeCategoryTree() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree()
        }
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                val primaryId = savedStateHandle.get<String>("primaryId")
                val topLevel = tree.filter { it.parentId == null }
                _state.update { it.copy(categoryTree = tree, topCategories = topLevel) }
                
                val initialIndex = if (primaryId != null) {
                    val idx = topLevel.indexOfFirst { it.id == primaryId }
                    if (idx >= 0) idx else 0
                } else {
                    0
                }

                if (topLevel.isNotEmpty() && _state.value.parentCategoryId.isEmpty()) {
                    onTabSelected(initialIndex)
                    savedStateHandle["primaryId"] = null
                } else if (_state.value.parentCategoryId.isNotEmpty()) {
                    if (primaryId != null && primaryId != _state.value.parentCategoryId) {
                        onTabSelected(initialIndex)
                        savedStateHandle["primaryId"] = null
                    } else {
                        val subs = tree.find { it.id == _state.value.parentCategoryId }?.children ?: emptyList()
                        _state.update { it.copy(subCategories = subs) }
                    }
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        val s = _state.value
        val topCategories = s.topCategories
        if (index in topCategories.indices) {
            val cat = topCategories[index]
            val subs = s.categoryTree.find { it.id == cat.id }?.children ?: emptyList()
            
            _state.update { 
                it.copy(
                    selectedTabIndex = index,
                    parentCategoryId = cat.id,
                    subCategories = subs,
                    selectedSubCategory = "all",
                    sort = "default"
                )
            }
            load(showFullLoading = true, page = 1)
        }
    }

    fun onSubCategory(c: String) {
        if (_state.value.selectedSubCategory == c) return
        _state.update { it.copy(selectedSubCategory = c) }
        load(showFullLoading = true, page = 1)
    }

    fun onSort(s: String) {
        if (_state.value.sort == s) return
        _state.update { it.copy(sort = s) }
        load(showFullLoading = true, page = 1)
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        load(showFullLoading = false, page = 1)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _state.update { it.copy(loadingMore = true) }
        load(showFullLoading = false, page = s.page + 1)
    }

    fun load(showFullLoading: Boolean = true, page: Int = 1) {
        val s = _state.value
        viewModelScope.launch {
            if (showFullLoading) _state.update { it.copy(loading = true, error = null) }
            
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(posts = emptyList()) }
            }

            val catParam = if (s.selectedSubCategory == "all" || s.selectedSubCategory.isBlank()) s.parentCategoryId else s.selectedSubCategory
            val sortParam = if (s.sort == "default") null else s.sort

            repo.posts(
                category = catParam.takeIf { it.isNotBlank() }, 
                publisherType = null,
                listingType = null,
                mine = false, 
                q = null, 
                sortBy = sortParam, 
                minPrice = null,
                maxPrice = null,
                attrFilter = null,
                page = page, 
                pageSize = 20
            )
                .onSuccess { resPage ->
                    val list = resPage.list
                    _state.update {
                        val newPosts = if (page == 1) list else it.posts + list
                        val hasMore = resPage.page * resPage.pageSize < resPage.total
                        it.copy(
                            loading = false, loadingMore = false, refreshing = false,
                            posts = newPosts, error = null,
                            page = page, hasMore = hasMore
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, loadingMore = false, refreshing = false, error = e.message ?: "加载失败") }
                }
        }
    }
}
