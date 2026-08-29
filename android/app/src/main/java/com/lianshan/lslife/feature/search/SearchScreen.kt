package com.qingyuan.lslife.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.ui.components.EmptyState
import com.qingyuan.lslife.ui.components.PostListCard
import com.qingyuan.lslife.ui.components.SkeletonCard

import androidx.compose.material.icons.filled.ArrowDropDown

private val sorts = listOf(
    "latest" to "最新发布",
    "price_asc" to "价格最低",
    "price_desc" to "价格最高",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var showCategorySelector by remember { mutableStateOf(false) }

    if (showCategorySelector && state.categoryTree.isNotEmpty()) {
        com.qingyuan.lslife.ui.components.GlobalCategorySelectorBottomSheet(
            categoryTree = state.categoryTree,
            onDismissRequest = { showCategorySelector = false },
            onCategorySelected = { categoryId ->
                showCategorySelector = false
                viewModel.updateCategory(if (categoryId == "all") null else categoryId)
            }
        )
    }

    LaunchedEffect(listState, state.loading, state.loadingMore, state.hasMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null) {
                    val totalItems = listState.layoutInfo.totalItemsCount
                    if (totalItems - lastIndex <= 3 && state.hasMore && !state.loading && !state.loadingMore) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    if (state.showFilterBottomSheet) {
        AdvancedFilterBottomSheet(
            schema = state.currentSchema,
            categoryTree = state.categoryTree,
            selectedCategory = state.category,
            publisherType = state.publisherType,
            listingType = state.listingType,
            minPrice = state.minPrice,
            maxPrice = state.maxPrice,
            attributesFilter = state.attributesFilter,
            aggregations = state.aggregations,
            onPublisherTypeChange = viewModel::updatePublisherType,
            onListingTypeChange = viewModel::updateListingType,
            onCategoryChange = { c -> viewModel.updateCategory(if (c == "all") null else c) },
            onPriceChange = viewModel::updatePrice,
            onAttributeToggle = viewModel::updateAttributeFilter,
            onReset = viewModel::clearAttributesFilter,
            onDismiss = { viewModel.setShowFilterBottomSheet(false) },
            onConfirm = { viewModel.setShowFilterBottomSheet(false) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = state.keyword,
                        onValueChange = viewModel::updateKeyword,
                        placeholder = { Text("搜索本地好物、岗位、商铺...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (state.keyword.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateKeyword("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "清空", tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.searchNow(state.keyword) }),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4-Pillar Filter Bar & Category Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val categoryPath = viewModel.getCategoryPathName(state.category) ?: "全部分类"
                    
                    ElevatedFilterChip(
                        selected = state.category != null,
                        onClick = { 
                            if (state.categoryTree.isNotEmpty()) {
                                showCategorySelector = true 
                            }
                        },
                        label = { Text(categoryPath, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (state.category != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.updateCategory(null) },
                            modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "清除分类", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 高级筛选按钮 (直接开启 BottomSheet)
                    val activeFiltersCount = state.attributesFilter.size + 
                        (if (state.category != null && state.category != "all") 1 else 0) +
                        (if (state.minPrice != null || state.maxPrice != null) 1 else 0) +
                        (if (state.publisherType != null) 1 else 0) +
                        (if (state.listingType != null) 1 else 0)
                    ElevatedFilterChip(
                        selected = activeFiltersCount > 0,
                        onClick = { viewModel.setShowFilterBottomSheet(true) },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("筛选")
                                if (activeFiltersCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(activeFiltersCount.toString(), color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.FilterList, contentDescription = "筛选", modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 排序与快捷价格
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sorts) { (id, name) ->
                        FilterChip(
                            selected = state.sortBy == id,
                            onClick = { viewModel.updateSort(id) },
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }

                    item {
                        val priceLabel = when {
                            state.minPrice != null && state.maxPrice != null -> "￥${state.minPrice!!.toInt()}-${state.maxPrice!!.toInt()}"
                            state.minPrice != null -> "￥${state.minPrice!!.toInt()}以上"
                            state.maxPrice != null -> "￥${state.maxPrice!!.toInt()}以内"
                            else -> "价格不限"
                        }
                        FilterChip(
                            selected = state.minPrice != null || state.maxPrice != null,
                            onClick = { viewModel.setShowFilterBottomSheet(true) },
                            label = { Text(priceLabel) }
                        )
                    }
                }
            }
        }

        // Body Content: Zero State vs Skeleton vs Results
        val isZeroState = state.keyword.isEmpty() && state.category == null && 
                state.attributesFilter.isEmpty() && state.minPrice == null && 
                state.maxPrice == null && state.posts.isEmpty() && !state.loading

        AnimatedVisibility(
            visible = isZeroState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // 搜索历史
                if (state.searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "最近搜索",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = viewModel::clearHistory, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "清空历史", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.searchHistory.forEach { kw ->
                            AssistChip(
                                onClick = { viewModel.searchNow(kw) },
                                label = { Text(kw) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 同城热搜榜
                Text(
                    text = "🔥 同城热搜榜",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.hotSearches.forEachIndexed { idx, kw ->
                        ElevatedAssistChip(
                            onClick = { viewModel.searchNow(kw) },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${idx + 1}.", 
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx < 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(kw)
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        if (!isZeroState) {
            if (state.loading && state.posts.isEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(4) {
                        SkeletonCard()
                    }
                }
            } else if (!state.loading && state.posts.isEmpty()) {
                EmptyState(
                    title = "未找到符合条件的同城信息",
                    subtitle = "换个同城热搜词或尝试调整高级筛选规配",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        PostListCard(
                            post = post, 
                            onClick = { onPostClick(post.id) }
                        )
                    }
                    if (state.loadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
