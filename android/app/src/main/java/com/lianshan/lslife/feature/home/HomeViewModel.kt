package com.qingyuan.lslife.feature.home

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.CategoryRepository
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.model.Banner
import com.qingyuan.lslife.core.model.CategoryNode
import com.qingyuan.lslife.core.model.CategorySchemaResponse

import com.qingyuan.lslife.core.model.Merchant
import com.qingyuan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.qingyuan.lslife.core.data.AddressManager
import com.qingyuan.lslife.core.data.AddressNode

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
    val showLocationPicker: Boolean = false,
    val addressNodes: List<AddressNode> = emptyList(),
    val isUgcMode: Boolean = true,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,

    val banners: List<Banner> = emptyList(),

    val selectedTab: String = "RECOMMENDED",
    val currentLocation: String = "清远市",
    val searchHotwords: List<String> = listOf("搜本地 靠谱保洁阿姨", "查看最新急售二手房", "找日结兼职工人", "精选同城美食"),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
    private val addressManager: AddressManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private var searchJob: Job? = null
    
    private val prefs = context.getSharedPreferences("home_cache", Context.MODE_PRIVATE)

    init {
        val cachedPostsStr = prefs.getString("cached_posts", null)
        var initialPosts: List<com.qingyuan.lslife.core.model.Post> = emptyList()
        if (!cachedPostsStr.isNullOrEmpty()) {
            try {
                initialPosts = kotlinx.serialization.json.Json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(com.qingyuan.lslife.core.model.Post.serializer()), 
                    cachedPostsStr
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (initialPosts.isNotEmpty()) {
            _state.update { it.copy(posts = initialPosts) }
        }

        viewModelScope.launch {
            _state.update { it.copy(addressNodes = addressManager.getAddresses()) }
        }
        observeCategoryTree()
        loadBanners()

        load()
    }

    fun setShowLocationPicker(show: Boolean) {
        _state.update { it.copy(showLocationPicker = show) }
    }

    fun updateLocation(location: String) {
        _state.update { it.copy(currentLocation = location, showLocationPicker = false) }
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

    fun onTabSelect(tab: String) {
        val sortParam = when (tab) {
            "RECOMMENDED" -> "recommend"
            "LATEST" -> "latest"
            "NEARBY" -> "latest"
            else -> "recommend"
        }
        _state.update { it.copy(selectedTab = tab, sort = sortParam) }
        load()
    }

    private fun loadBanners() {
        viewModelScope.launch {
            repo.getBanners().onSuccess { b ->
                _state.update { it.copy(banners = b) }
            }
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
            
            val isDefaultView = s.category == "all" && s.sort == "default" && s.query.isBlank() && s.attributesFilter.isEmpty() && s.publisherType == null && s.listingType == null
            if (page == 1 && showFullLoading) {
                if (!(isDefaultView && s.posts.isNotEmpty())) {
                    _state.update { it.copy(merchants = emptyList(), posts = emptyList()) }
                }
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
                postType = "CLASSIFIED", // Only fetch Classified for the first tab
                page = page, 
                pageSize = 20
            )
                .onSuccess { resPage ->
                    val list = resPage.list
                    if (page == 1 && catParam == null && sortParam == null && queryParam == null && s.attributesFilter.isEmpty() && s.publisherType == null && s.listingType == null) {
                        try {
                            val json = kotlinx.serialization.json.Json.encodeToString(
                                kotlinx.serialization.builtins.ListSerializer(com.qingyuan.lslife.core.model.Post.serializer()), 
                                list
                            )
                            prefs.edit().putString("cached_posts", json).apply()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
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




}
