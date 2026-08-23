package com.lianshan.lslife.feature.search

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CategorySchemaResponse
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val keyword: String = "",
    val category: String? = null,
    val categoryTree: List<com.lianshan.lslife.core.model.CategoryNode> = emptyList(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortBy: String = "latest", // latest, price_asc, price_desc
    val currentSchema: CategorySchemaResponse? = null,
    val publisherType: String? = null,
    val listingType: String? = null,
    val attributesFilter: Map<String, Set<String>> = emptyMap(),
    val searchHistory: List<String> = emptyList(),
    val hotSearches: List<String> = listOf("iPhone 15", "日常保洁", "两室一厅", "寒暑假工", "小面包车", "旺铺转让", "全新手机", "送货搬家"),
    val showFilterBottomSheet: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 1,
    val posts: List<Post> = emptyList(),
    val aggregations: Map<String, Map<String, Int>> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: LsRepository,
    private val app: Application
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    private var searchJob: Job? = null
    private val prefs by lazy { app.getSharedPreferences("lslife_search_history", Context.MODE_PRIVATE) }

    init {
        loadHistory()
        loadCategoryTree()
    }

    private fun loadCategoryTree() {
        viewModelScope.launch {
            repo.categoryTree().onSuccess { tree ->
                _state.update { it.copy(categoryTree = tree) }
            }
        }
    }

    fun getCategoryPathName(categoryId: String?): String? {
        if (categoryId == null) return null
        val tree = _state.value.categoryTree
        if (tree.isEmpty()) return null
        return findCategoryPath(tree, categoryId, "")
    }

    private fun findCategoryPath(nodes: List<com.lianshan.lslife.core.model.CategoryNode>, targetId: String, currentPath: String): String? {
        for (node in nodes) {
            val path = if (currentPath.isEmpty()) node.name else "$currentPath > ${node.name}"
            if (node.id == targetId) return path
            val found = findCategoryPath(node.children, targetId, path)
            if (found != null) return found
        }
        return null
    }

    private fun loadHistory() {
        val historyStr = prefs.getString("history_list", "") ?: ""
        val list = if (historyStr.isBlank()) emptyList() else historyStr.split("||")
        _state.update { it.copy(searchHistory = list) }
    }

    fun saveHistory(keyword: String) {
        val k = keyword.trim()
        if (k.isBlank()) return
        val current = _state.value.searchHistory.toMutableList()
        current.remove(k)
        current.add(0, k)
        val trimmed = current.take(10)
        prefs.edit().putString("history_list", trimmed.joinToString("||")).apply()
        _state.update { it.copy(searchHistory = trimmed) }
    }

    fun removeHistory(keyword: String) {
        val current = _state.value.searchHistory.filter { it != keyword }
        prefs.edit().putString("history_list", current.joinToString("||")).apply()
        _state.update { it.copy(searchHistory = current) }
    }

    fun clearHistory() {
        prefs.edit().remove("history_list").apply()
        _state.update { it.copy(searchHistory = emptyList()) }
    }

    fun updateKeyword(k: String) {
        _state.update { it.copy(keyword = k) }
        debouncedSearch()
    }

    fun searchNow(k: String) {
        _state.update { it.copy(keyword = k) }
        saveHistory(k)
        searchJob?.cancel()
        load(page = 1, showFullLoading = true)
    }

    fun updateCategory(c: String?) {
        _state.update { it.copy(category = c) }
        viewModelScope.launch {
            if (c == null) {
                _state.update { it.copy(currentSchema = null, attributesFilter = emptyMap()) }
            } else {
                repo.categorySchema(c).onSuccess { schema ->
                    _state.update { it.copy(currentSchema = schema, attributesFilter = emptyMap()) }
                }
            }
            load(page = 1)
        }
    }

    fun updatePrice(min: Double?, max: Double?) {
        _state.update { it.copy(minPrice = min, maxPrice = max) }
        load(page = 1)
    }

    fun updateSort(sort: String) {
        _state.update { it.copy(sortBy = sort) }
        load(page = 1)
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
        load(page = 1)
    }

    fun updatePublisherType(type: String?) {
        _state.update { it.copy(publisherType = type) }
        load(page = 1)
    }

    fun updateListingType(type: String?) {
        _state.update { it.copy(listingType = type) }
        load(page = 1)
    }

    fun clearAttributesFilter() {
        _state.update { it.copy(attributesFilter = emptyMap(), minPrice = null, maxPrice = null, publisherType = null, listingType = null) }
        load(page = 1)
    }

    fun resetAllFilters() {
        _state.update { 
            it.copy(
                category = null,
                minPrice = null,
                maxPrice = null,
                sortBy = "latest",
                publisherType = null,
                listingType = null,
                currentSchema = null,
                attributesFilter = emptyMap()
            )
        }
        load(page = 1)
    }

    fun setShowFilterBottomSheet(show: Boolean) {
        _state.update { it.copy(showFilterBottomSheet = show) }
    }

    private fun debouncedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (_state.value.keyword.isNotBlank()) {
                saveHistory(_state.value.keyword)
            }
            load(page = 1, showFullLoading = false)
        }
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        load(page = 1, showFullLoading = false)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _state.update { it.copy(loadingMore = true) }
        load(page = s.page + 1, showFullLoading = false)
    }

    private fun load(page: Int, showFullLoading: Boolean = true) {
        val s = _state.value
        viewModelScope.launch {
            if (showFullLoading) {
                _state.update { it.copy(loading = true, error = null) }
            }
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(posts = emptyList()) }
            }

            repo.posts(
                category = s.category,
                publisherType = s.publisherType,
                listingType = s.listingType,
                mine = false,
                q = s.keyword.takeIf { it.isNotBlank() },
                minPrice = s.minPrice,
                maxPrice = s.maxPrice,
                sortBy = s.sortBy,
                attrFilter = s.attributesFilter.takeIf { it.isNotEmpty() }?.mapValues { it.value.joinToString("||") },
                page = page,
                pageSize = 20
            ).onSuccess { resPage ->
                _state.update {
                    val newPosts = if (page == 1) resPage.list else it.posts + resPage.list
                    val hasMore = resPage.page * resPage.pageSize < resPage.total
                    it.copy(
                        loading = false, loadingMore = false, refreshing = false, error = null,
                        posts = newPosts, page = page, hasMore = hasMore,
                        aggregations = if (page == 1) resPage.aggregations else it.aggregations
                    )
                }
            }.onFailure { e ->
                _state.update { 
                    it.copy(
                        loading = false, loadingMore = false, refreshing = false, 
                        error = e.message ?: "加载失败"
                    ) 
                }
            }
        }
    }


}
