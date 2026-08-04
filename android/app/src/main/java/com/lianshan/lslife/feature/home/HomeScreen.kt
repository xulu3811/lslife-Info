package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.BrandHero
import com.lianshan.lslife.ui.components.CategoryPill
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.MerchantListCard
import com.lianshan.lslife.ui.components.PostListCard
import com.lianshan.lslife.ui.components.RecommendCard
import com.lianshan.lslife.ui.components.SectionHeader
import com.lianshan.lslife.ui.components.WarmSearchField
import com.lianshan.lslife.ui.components.SkeletonCard
import com.lianshan.lslife.ui.components.CategoryIconView
import com.lianshan.lslife.ui.theme.Dimens
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lianshan.lslife.feature.search.AdvancedFilterBottomSheet
import com.lianshan.lslife.ui.SessionViewModel

private data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String,
    val iconUrl: String? = null
)

private val defaultCategories = listOf(
    CategoryItem("cat_idle", "个人闲置", "shopping-bag", "/assets/icons/3d_flat_secondhand.png"),
    CategoryItem("cat_service", "家政保洁", "cleaning-services", "/assets/icons/3d_flat_cleaning.png"),
    CategoryItem("cat_veggies", "同城生鲜", "shopping-basket", "/assets/icons/3d_flat_fresh_food.png"),
    CategoryItem("cat_maintenance", "水电维修", "build", "/assets/icons/3d_flat_repair.png"),
    CategoryItem("cat_dining", "餐饮娱乐", "restaurant", "/assets/icons/3d_flat_dining.png"),
    CategoryItem("cat_house_sale", "二手房", "home", "/assets/icons/3d_flat_housing.png"),
    CategoryItem("cat_house_rent", "租房", "home", "/assets/icons/3d_flat_house_short.png"),
    CategoryItem("cat_job", "求职招聘", "work", "/assets/icons/3d_flat_jobs.png"),
    CategoryItem("cat_car_rental", "拼车/租车", "local-shipping", "/assets/icons/3d_flat_car_rental.png"),
    CategoryItem("cat_education", "教育培训", "school", "/assets/icons/3d_flat_education.png"),
)

private val sorts = listOf(
    "default" to "推荐",
    "latest" to "最新",
    "price_asc" to "价格最低",
    "price_desc" to "价格最高",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenMerchant: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMessageClick: () -> Unit,
    onNavigateToCategory: (String, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unreadCount by sessionViewModel.unreadCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            Toast.makeText(context, "扫码结果: ${result.contents}", Toast.LENGTH_LONG).show()
        }
    }
    val gridState = rememberLazyStaggeredGridState()

    val displayCategories = androidx.compose.runtime.remember(state.categoryTree) {
        val list = if (state.categoryTree.isEmpty()) {
            defaultCategories.toMutableList()
        } else {
            state.categoryTree.map { node ->
                CategoryItem(
                    id = node.id,
                    name = node.name,
                    icon = node.icon ?: "📁",
                    iconUrl = node.iconUrl
                )
            }.toMutableList()
        }
        list
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
            onPublisherTypeChange = viewModel::updatePublisherType,
            onListingTypeChange = viewModel::updateListingType,
            onCategoryChange = { c -> viewModel.onCategory(c ?: "all") },
            onPriceChange = viewModel::updatePrice,
            onAttributeToggle = viewModel::updateAttributeFilter,
            onReset = viewModel::clearAttributesFilter,
            onDismiss = { viewModel.setShowFilterBottomSheet(false) },
            onConfirm = { viewModel.setShowFilterBottomSheet(false) }
        )
    }

    // Infinite scroll
    LaunchedEffect(gridState, state.loading, state.loadingMore, state.hasMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null) {
                    val totalItems = gridState.layoutInfo.totalItemsCount
                    // Load more when user scrolls to the last 3 items
                    if (totalItems - lastIndex <= 3 && state.hasMore && !state.loading && !state.loadingMore) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp), // Increased top padding to avoid status bar overlap
    ) {
        Column(modifier = Modifier.padding(horizontal = Dimens.lg)) {
            // Location and Action Icons (Image 3 Style)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.sm), // Reduced bottom padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp) // Slightly smaller icon
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "连山壮族瑶族自治县 >",
                    style = MaterialTheme.typography.labelLarge, // Smaller font size
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                if (unreadCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge { Text(unreadCount.toString()) }
                        },
                        modifier = Modifier.padding(end = Dimens.sm)
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "消息",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp).clickable { onMessageClick() }
                        )
                    }
                } else {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "消息",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp).clickable { onMessageClick() }.padding(end = Dimens.sm)
                    )
                }
            }
            
            // Search Bar (Image 3 Style with Image 1 colors)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clickable { onSearchClick() },
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = Dimens.md, end = 4.dp)
                ) {
                    Text(
                        text = "搜索本地商户、商品、服务",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111111))
                            .clickable { onSearchClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "搜索",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.xs)) // Reduced from sm
            
        } // Close the top padding column

        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(
                    start = Dimens.lg,
                    end = Dimens.lg,
                    top = 0.dp,
                    bottom = Dimens.xl,
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.listGap),
                verticalItemSpacing = Dimens.listGap,
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 0.dp)
                    ) {
                        val commerceIds = setOf("cat_idle", "cat_veggies", "cat_service", "cat_maintenance", "cat_dining")
                        val commerceItems = displayCategories.filter { it.id in commerceIds }
                        val infoIds = setOf("cat_house_sale", "cat_house_rent", "cat_job", "cat_car_rental", "cat_education")
                        val infoItems = displayCategories.filter { it.id in infoIds }

                        if (commerceItems.isNotEmpty() || infoItems.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                                        val w = size.width
                                        val h = size.height
                                        val yLeft = h * 0.42f
                                        val yRight = h * 0.58f

                                        // 底部：同城发布的蓝底
                                        drawRect(color = Color(0xFFF2F8FF), size = androidx.compose.ui.geometry.Size(w, h))

                                        // 顶部：同城交易的粉底，带有一条 S 型优雅切割曲线
                                        val pinkPath = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(0f, 0f)
                                            lineTo(w, 0f)
                                            lineTo(w, yRight)
                                            cubicTo(
                                                w * 0.5f, yRight,
                                                w * 0.5f, yLeft,
                                                0f, yLeft
                                            )
                                            close()
                                        }
                                        drawPath(pinkPath, color = Color(0xFFFFF5F2))

                                        // 中间切割处的白色描边
                                        val wavePath = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(0f, yLeft)
                                            cubicTo(
                                                w * 0.5f, yLeft,
                                                w * 0.5f, yRight,
                                                w, yRight
                                            )
                                        }
                                        drawPath(
                                            wavePath, 
                                            color = Color.White, 
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                                        )
                                    }

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)) {
                                            commerceItems.forEach { item ->
                                                CategoryItemView(item, state.category) { id, name -> onNavigateToCategory(id, name) }
                                            }
                                            repeat(5 - commerceItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "同城发布",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    color = Color(0xFF0D47A1)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "生活服务",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    color = Color(0xFFBF360C)
                                                )
                                            }
                                        }

                                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)) {
                                            infoItems.forEach { item ->
                                                CategoryItemView(item, state.category) { id, name -> onNavigateToCategory(id, name) }
                                            }
                                            repeat(5 - infoItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.sm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.sm),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            sorts.forEach { (id, name) ->
                                val selected = state.sort == id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { viewModel.onSort(id) }.padding(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onBackground else androidx.compose.ui.graphics.Color(0xFF888888)
                                    )
                                    if (selected) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .height(3.dp)
                                                .width(14.dp)
                                                .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(999.dp))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            val activeFiltersCount = state.attributesFilter.size + 
                                (if (state.category != "all") 1 else 0) +
                                (if (state.minPrice != null || state.maxPrice != null) 1 else 0) +
                                (if (state.publisherType != null) 1 else 0) +
                                (if (state.listingType != null) 1 else 0)
                                
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.setShowFilterBottomSheet(true) }.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "筛选",
                                    fontSize = 13.sp,
                                    fontWeight = if (activeFiltersCount > 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activeFiltersCount > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Filled.FilterList, contentDescription = "筛选", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (state.loading && state.merchants.isEmpty() && state.posts.isEmpty()) {
                    items(6) {
                        SkeletonCard()
                    }
                } else if (state.error != null && state.merchants.isEmpty() && state.posts.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        ErrorBox(state.error!!, onRetry = { viewModel.load() })
                    }
                } else {
                    if (!state.isUgcMode && state.recommended.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            SectionHeader(title = "今日推荐")
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                                lazyItems(state.recommended) { m ->
                                    RecommendCard(m) { onOpenMerchant(m.id) }
                                }
                            }
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Spacer(modifier = Modifier.height(Dimens.md))
                            Text(
                                "周边服务推荐",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(Dimens.sm))
                        }
                    }

                    if (state.isUgcMode) {
                        items(state.posts, key = { it.id }) { post ->
                            if (post.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH || post.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO) {
                                com.lianshan.lslife.ui.components.InfoPublishCard(
                                    post = post,
                                    onClick = { onOpenPost(post.id) },
                                    onPhoneClick = { 
                                        if (post.contactPhone.isNullOrBlank()) {
                                            android.widget.Toast.makeText(context, "发布者未留电话", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "拨打电话: ${post.contactPhone}", android.widget.Toast.LENGTH_SHORT).show()
                                            // TODO: Intent to dialer
                                        }
                                    },
                                    onChatClick = {
                                        android.widget.Toast.makeText(context, "联系发布者私聊", android.widget.Toast.LENGTH_SHORT).show()
                                        // TODO: navigate to chat
                                    }
                                )
                            } else {
                                com.lianshan.lslife.ui.components.O2OProductCard(
                                    post = post, 
                                    onClick = { onOpenPost(post.id) },
                                    onAddCartClick = { 
                                        viewModel.addToCart(
                                            postId = post.id,
                                            onSuccess = { android.widget.Toast.makeText(context, "已加入购物车", android.widget.Toast.LENGTH_SHORT).show() },
                                            onError = { msg -> android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show() }
                                        )
                                    }
                                )
                            }
                        }
                        if (state.posts.isEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                com.lianshan.lslife.ui.components.EmptyState(
                                    title = if (state.category == "all" || state.category.isEmpty()) "暂无同城推荐内容" else "还没有发布内容",
                                    subtitle = if (state.category == "all" || state.category.isEmpty()) "快去「发布」发一条闲置或同城动态吧！" else "去「发布」发一条同城动态吧",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Dimens.xxl * 6),
                                )
                            }
                        }
                    } else {
                        items(state.merchants, key = { it.id }, span = { StaggeredGridItemSpan.FullLine }) { m ->
                            com.lianshan.lslife.ui.components.MerchantListCard(m) { onOpenMerchant(m.id) }
                        }
                        if (state.merchants.isEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                com.lianshan.lslife.ui.components.EmptyState(
                                    title = "没有找到匹配的商户",
                                    subtitle = "换个关键词或分类试试",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Dimens.xxl * 6),
                                )
                            }
                        }
                    }

                    if (state.loadingMore) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.md),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    } else if (!state.hasMore && (!state.isUgcMode && state.merchants.isNotEmpty() || state.isUgcMode && state.posts.isNotEmpty())) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.lg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "—— 到底了 ——",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CategoryItemView(
    item: CategoryItem,
    selectedCategory: String,
    onClick: (String, String) -> Unit
) {
    val selected = selectedCategory == item.id
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick(item.id, item.name) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp) // Appropriately larger
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant, // Always light gray
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            CategoryIconView(
                iconUrl = item.iconUrl,
                iconName = item.icon,
                categoryName = item.name,
                size = 36.dp, // Larger icon
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp)) // Slight increase in spacing
        Text(
            text = item.name,
            fontSize = 12.sp, // slightly larger text
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
