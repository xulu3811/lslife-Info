package com.lianshan.lslife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.CategoryRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.CategorySchemaResponse
import com.lianshan.lslife.core.model.Merchant
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 同城信息类目（走 /posts），其余走商家 */
val UGC_CATEGORIES = setOf(
    "second_hand", "job", "part_time", "house", "secondhand_house", "shop_rent", "housekeeping", "maintenance", "moving", "veggies",
)

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val categoryTree: List<CategoryNode> = emptyList(),
    val merchants: List<Merchant> = emptyList(),
    val posts: List<Post> = emptyList(),
    val recommended: List<Merchant> = emptyList(),
    val query: String = "",
    val category: String = "all",
    val sort: String = "default",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val publisherType: String? = null,
    val listingType: String? = null,
    val attributesFilter: Map<String, Set<String>> = emptyMap(),
    val currentSchema: CategorySchemaResponse? = null,
    val showFilterBottomSheet: Boolean = false,
    val isUgcMode: Boolean = true,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val momentPosts: List<Post> = emptyList(),
    val momentPage: Int = 1,
    val momentHasMore: Boolean = true,
    val momentLoadingMore: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private var searchJob: Job? = null

    init {
        observeCategoryTree()
        load()
    }

    private fun observeCategoryTree() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree()
        }
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                _state.update { it.copy(categoryTree = tree) }
            }
        }
    }

    fun onQueryChange(v: String) {
        _state.update { it.copy(query = v) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            load(showFullLoading = false)
        }
    }

    fun onCategory(c: String) {
        val newCat = if (_state.value.category == c && c != "all") "all" else c
        _state.update { it.copy(category = newCat, isUgcMode = true) }
        viewModelScope.launch {
            if (newCat == "all") {
                _state.update { it.copy(currentSchema = null, attributesFilter = emptyMap()) }
            } else {
                repo.categorySchema(newCat).onSuccess { schema ->
                    _state.update { it.copy(currentSchema = schema, attributesFilter = emptyMap()) }
                }
            }
            load()
        }
    }

    fun onSort(s: String) {
        _state.update { it.copy(sort = s) }
        load()
    }

    fun updatePrice(min: Double?, max: Double?) {
        _state.update { it.copy(minPrice = min, maxPrice = max) }
        load()
    }

    fun updateAttributeFilter(key: String, value: String) {
        val current = _state.value.attributesFilter.toMutableMap()
        val currentSet = (current[key] ?: emptySet()).toMutableSet()
        if (currentSet.contains(value)) {
            currentSet.remove(value)
            if (currentSet.isEmpty()) current.remove(key) else current[key] = currentSet
        } else {
            currentSet.add(value)
            current[key] = currentSet
        }
        _state.update { it.copy(attributesFilter = current) }
        load()
    }

    fun updatePublisherType(type: String?) {
        _state.update { it.copy(publisherType = type) }
        load()
    }

    fun updateListingType(type: String?) {
        _state.update { it.copy(listingType = type) }
        load()
    }

    fun clearAttributesFilter() {
        _state.update { it.copy(attributesFilter = emptyMap(), minPrice = null, maxPrice = null, publisherType = null, listingType = null) }
        load()
    }

    fun setShowFilterBottomSheet(show: Boolean) {
        _state.update { it.copy(showFilterBottomSheet = show) }
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        load(showFullLoading = false, page = 1)
        loadMoments(page = 1)
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
            if (showFullLoading) _state.update { it.copy(loading = true, error = null, isUgcMode = true) }
            
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(merchants = emptyList(), posts = emptyList(), momentPosts = emptyList()) }
                loadMoments(page = 1)
            }

            val catParam = if (s.category == "all" || s.category.isBlank()) null else s.category
            val sortParam = if (s.sort == "default") null else s.sort
            val queryParam = s.query.takeIf { it.isNotBlank() }

            repo.posts(
                category = catParam, 
                publisherType = s.publisherType,
                listingType = s.listingType,
                mine = false, 
                q = queryParam, 
                sortBy = sortParam, 
                minPrice = s.minPrice,
                maxPrice = s.maxPrice,
                attrFilter = s.attributesFilter.takeIf { it.isNotEmpty() }?.mapValues { it.value.joinToString("||") },
                postType = "COMMERCE", // Only fetch Commerce for the first tab
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
                            posts = newPosts, merchants = emptyList(), error = null, isUgcMode = true,
                            page = page, hasMore = hasMore
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, loadingMore = false, refreshing = false, error = e.message ?: "加载失败", isUgcMode = true) }
                }
            
            if (page == 1) {
                repo.recommended().onSuccess { rec -> _state.update { it.copy(recommended = rec) } }
            }
        }
    }

    fun addToCart(postId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val post = _state.value.posts.find { it.id == postId } ?: return
        if (post.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH || post.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO) {
            onError("信息类服务不支持加入购物车，请直接联系发布者")
            return
        }
        viewModelScope.launch {
            repo.upsertCart(postId = postId, quantity = 1)
            onSuccess()
        }
    }

    fun loadMoreMoments() {
        val s = _state.value
        if (s.momentLoadingMore || !s.momentHasMore) return
        _state.update { it.copy(momentLoadingMore = true) }
        loadMoments(page = s.momentPage + 1)
    }

    private fun loadMoments(page: Int = 1) {
        viewModelScope.launch {
            repo.posts(
                postType = "MOMENT",
                page = page,
                pageSize = 20
            ).onSuccess { resPage ->
                val list = resPage.list
                _state.update {
                    val newPosts = if (page == 1) list else it.momentPosts + list
                    val hasMore = resPage.page * resPage.pageSize < resPage.total
                    it.copy(
                        momentPosts = newPosts,
                        momentPage = page, 
                        momentHasMore = hasMore,
                        momentLoadingMore = false
                    )
                }
            }.onFailure {
                _state.update { it.copy(momentLoadingMore = false) }
            }
        }
    }
}
