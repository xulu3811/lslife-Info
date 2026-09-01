package com.qingyuan.lslife.feature.home

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.Banner
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.feature.search.AdvancedFilterBottomSheet
import com.qingyuan.lslife.ui.SessionViewModel
import com.qingyuan.lslife.ui.components.*
import com.qingyuan.lslife.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

private data class CategoryBadgeItem(
    val id: String,
    val name: String,
    val iconUrl: String,
    val badge: String? = null
)

private val page1Categories = listOf(
    CategoryBadgeItem("cat_2_service", "家政/护理", "android.resource://com.qingyuan.lslife/drawable/ic_category_service"),
    CategoryBadgeItem("cat_3_repair", "便民维修", "android.resource://com.qingyuan.lslife/drawable/ic_category_repair"),
    CategoryBadgeItem("cat_4_fresh", "同城生鲜", "android.resource://com.qingyuan.lslife/drawable/ic_category_fresh"),
    CategoryBadgeItem("cat_5_rent", "房屋出租", "android.resource://com.qingyuan.lslife/drawable/ic_category_rent"),
    CategoryBadgeItem("cat_6_sale", "二手房产", "android.resource://com.qingyuan.lslife/drawable/ic_category_sale"),
    CategoryBadgeItem("cat_7_carpool", "拼车/租车", "android.resource://com.qingyuan.lslife/drawable/ic_category_carpool"),
    CategoryBadgeItem("cat_8_job", "招聘求职", "android.resource://com.qingyuan.lslife/drawable/ic_category_job"),
    CategoryBadgeItem("cat_9_life", "吃喝玩乐", "android.resource://com.qingyuan.lslife/drawable/ic_category_life"),
    CategoryBadgeItem("cat_10_edu", "教育培训", "android.resource://com.qingyuan.lslife/drawable/ic_category_edu"),
    CategoryBadgeItem("cat_1_idle", "个人闲置", "android.resource://com.qingyuan.lslife/drawable/ic_category_idle"),
)

private val page2Categories = emptyList<CategoryBadgeItem>()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onOpenMerchant: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMessageClick: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToCategoryTab: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val unreadCount by sessionViewModel.unreadCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                          permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                coroutineScope.launch {
                    val town = com.qingyuan.lslife.core.utils.LocationHelper.getCurrentTown(context)
                    if (town != null) {
                        viewModel.updateLocation(town)
                    } else {
                        Toast.makeText(context, "自动定位失败，请手动选择", Toast.LENGTH_SHORT).show()
                        viewModel.setShowLocationPicker(true)
                    }
                }
            } else {
                Toast.makeText(context, "未获得定位权限，请手动选择", Toast.LENGTH_SHORT).show()
                viewModel.setShowLocationPicker(true)
            }
        }
    )

    // 初次启动自动请求定位或静默获取定位
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val town = com.qingyuan.lslife.core.utils.LocationHelper.getCurrentTown(context)
            if (town != null) {
                viewModel.updateLocation(town)
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // 搜索热词自动轮播
    var hotwordIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.searchHotwords) {
        if (state.searchHotwords.isNotEmpty()) {
            while (true) {
                delay(3500)
                hotwordIndex = (hotwordIndex + 1) % state.searchHotwords.size
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

    if (state.showLocationPicker && state.addressNodes.isNotEmpty()) {
        com.qingyuan.lslife.ui.components.AddressPickerBottomSheet(
            addressNodes = state.addressNodes,
            onDismissRequest = { viewModel.setShowLocationPicker(false) },
            onAddressSelected = { viewModel.updateLocation(it.split("-").lastOrNull() ?: it) }
        )
    }

    LaunchedEffect(gridState, state.loading, state.loadingMore, state.hasMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null) {
                    val totalItems = gridState.layoutInfo.totalItemsCount
                    if (totalItems - lastIndex <= 3 && state.hasMore && !state.loading && !state.loadingMore) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFFF3F5F8))
            .statusBarsPadding()
    ) {
        // 第一层：顶部搜索与本地化头栏
        TopSearchHeaderBar(
            locationText = state.currentLocation,
            hotword = state.searchHotwords.getOrElse(hotwordIndex) { "搜索本地商户、商品、服务" },
            unreadCount = unreadCount,
            onLocationClick = { 
                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasFine || hasCoarse) {
                    coroutineScope.launch {
                        Toast.makeText(context, "正在获取位置...", Toast.LENGTH_SHORT).show()
                        val town = com.qingyuan.lslife.core.utils.LocationHelper.getCurrentTown(context)
                        if (town != null) {
                            viewModel.updateLocation(town)
                        } else {
                            Toast.makeText(context, "定位失败，请手动选择", Toast.LENGTH_SHORT).show()
                            viewModel.setShowLocationPicker(true)
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            },
            onSearchClick = onSearchClick,
            onMessageClick = onMessageClick
        )

        val tabs = listOf("首页", "同城动态")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val coroutineScope = rememberCoroutineScope()

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.height(36.dp), // 进一步降低高度
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(pagerState.currentPage)
                        .padding(horizontal = 48.dp) // 缩短指示器宽度
                        .clip(RoundedCornerShape(50)),
                    height = 3.dp, // 细化至3dp
                    color = Color(0xFF4285F4)
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    modifier = Modifier.height(36.dp),
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = title,
                                fontSize = 14.sp, // 缩小字体
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant // 平滑的灰度过渡
                            )
                            if (index == 1) {
                                // 同城动态红点的呼吸动画
                                val infiniteTransition = rememberInfiniteTransition(label = "badge_breathing")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "badge_alpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .offset(x = 2.dp, y = 0.dp)
                                        .graphicsLayer { this.alpha = alpha }
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    PullToRefreshBox(
                        isRefreshing = state.refreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            state = gridState,
                            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = Dimens.xl),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            modifier = Modifier.fillMaxSize(),
                        ) {


                            // 第三层：金刚区导航 (固定 10 宫格)
                            item(span = StaggeredGridItemSpan.FullLine) {
                                KingkongCategoryGrid(
                                    onCategoryClick = onNavigateToCategory
                                )
                            }



                            // 第五层：核心瀑布流 Tab 栏 ([推荐] [最新] [附近])
                            item(span = StaggeredGridItemSpan.FullLine) {
                                FeedTabHeader(
                                    selectedTab = state.selectedTab,
                                    onTabSelect = viewModel::onTabSelect,
                                    onFilterClick = { viewModel.setShowFilterBottomSheet(true) }
                                )
                            }

                            // 第五层：差异化卡片展示 (B端认证商家 vs C端个人)
                            if (state.loading && state.posts.isEmpty()) {
                                items(6) { SkeletonCard() }
                            } else if (state.error != null && state.posts.isEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) { ErrorBox(state.error!!, onRetry = { viewModel.load() }) }
                            } else {
                                items(state.posts, key = { it.id }) { post ->
                                    StandardFeedCard(
                                        post = post,
                                        onClick = { onOpenPost(post.id) },
                                        onChatClick = { onOpenPost(post.id) }
                                    )
                                }

                                if (state.posts.isEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        EmptyState(
                                            title = "暂无相关同城信息",
                                            subtitle = "去「发布」发一条同城服务或需求吧！",
                                            modifier = Modifier.fillMaxWidth().height(Dimens.xxl * 6),
                                        )
                                    }
                                }

                                if (state.loadingMore) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.md), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                        }
                                    }
                                } else if (!state.hasMore && state.posts.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.lg), contentAlignment = Alignment.Center) {
                                            Text("—— 同城清远，贴心服务 ——", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val dynamicsViewModel: DynamicsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    CityDynamicsScreen(
                        viewModel = dynamicsViewModel,
                        onPostClick = { onOpenPost(it) },
                        onChatClick = { onOpenPost(it) } // Navigate to chat or post detail
                    )
                }
            }
        }
    }
}

/** 第一层：顶部搜索与本地化头栏 */
@Composable
private fun TopSearchHeaderBar(
    locationText: String,
    hotword: String,
    unreadCount: Int,
    onLocationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMessageClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.md, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 定位按钮
        Row(
            modifier = Modifier
                .clickable { onLocationClick() }
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFF34A853),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            
            val displayLocation = locationText.split("-").lastOrNull()?.take(5) ?: locationText
            Text(
                text = displayLocation,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 搜索框 + 热词轮播
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                AnimatedContent(
                    targetState = hotword,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    },
                    label = "hotwordAnimation"
                ) { targetText ->
                    Text(
                        text = targetText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 消息图标与未读 Count
        Box(modifier = Modifier.clickable { onMessageClick() }) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Messages",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(26.dp)
            )
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** 第二层：头部大 Banner 轮播 (品牌曝光位) */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeBannerCarousel(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onBannerClick(banner) }
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // 蒙层标题
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = banner.title,
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // 底部圆点指示器
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(banners.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (active) 12.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (active) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.7f))
                )
            }
        }
    }
}

/** 第三层：金刚区导航 (固定 10 宫格) */
@Composable
private fun KingkongCategoryGrid(
    onCategoryClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val row1 = page1Categories.take(5)
            val row2 = page1Categories.drop(5).take(5)

            Row(modifier = Modifier.fillMaxWidth()) {
                row1.forEach { cat ->
                    KingkongItemView(cat, modifier = Modifier.weight(1f)) { id ->
                        onCategoryClick(id)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                row2.forEach { cat ->
                    KingkongItemView(cat, modifier = Modifier.weight(1f)) { id ->
                        onCategoryClick(id)
                    }
                }
            }
        }
    }
}

@Composable
private fun KingkongItemView(
    cat: CategoryBadgeItem,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick(cat.id) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 移除灰底圆环，采用无边框设计凸显高清图片
        // 针对某些长宽比较特殊、视觉上显得偏小的实物图，进行代码层的智能放大
        val scaleFactor = when {
            cat.name.contains("二手") -> 1.4f // 放大二手房产
            cat.name.contains("拼车") -> 1.3f // 放大拼车租车
            else -> 1.0f
        }
        
        AsyncImage(
            model = cat.iconUrl,
            contentDescription = cat.name,
            modifier = Modifier.size(48.dp).scale(scaleFactor)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cat.name,
            fontSize = 12.sp,
            color = Color(0xFF374151), // 更深更清晰的文字颜色
            fontWeight = FontWeight.Bold, // 加粗以平衡大图标
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



/** 第五层：Feed 切换 Tab 栏 */
@Composable
private fun FeedTabHeader(
    selectedTab: String,
    onTabSelect: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    val tabs = listOf(
        "RECOMMENDED" to "推荐",
        "LATEST" to "最新",
        "NEARBY" to "附近"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // 与金刚区对齐
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White, // 纯白卡片，无阴影
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp), // 舒适内边距
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { (key, name) ->
                        val selected = selectedTab == key
                        Column(
                            modifier = Modifier.clickable { onTabSelect(key) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = name,
                                fontSize = if (selected) 16.sp else 14.sp, // 增大字体
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(16.dp, 3.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clickable { onFilterClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("筛选", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // 缩小字体
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp) // 缩小图标
                    )
                }
            }
        }
    }
}

/** 第五层卡片：标准图文 Feed 卡片 (商品/服务) */
@Composable
private fun StandardFeedCard(
    post: Post,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (post.isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            val imageUrl = post.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f, matchHeightConstraintsFirst = false) // 限制为 3:2 比例
                        .background(androidx.compose.ui.graphics.Color(0xFFF3F5F8)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl + "?x-oss-process=image/resize,w_500",
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop, // 确保图片裁剪不缩放变形
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = post.title ?: "同城发布",
                    fontSize = 14.sp, // 增大至14.sp，提升层级
                    fontWeight = FontWeight.Bold, // 保持粗体
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp)) // 内边距更舒展

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarUrl = post.user?.avatar ?: ""
                    val authorName = post.user?.nickname ?: "清远用户"
                    
                    com.qingyuan.lslife.ui.components.GoogleAvatar(
                        url = avatarUrl,
                        size = 20.dp
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = authorName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (post.sellerType == "MERCHANT") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "金牌", fontSize = 9.sp, color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (post.isUrgent) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE53935), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "急售", fontSize = 9.sp, color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp)) // 增大联系按钮与前面内容的间距
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier
                            .height(26.dp) // 按钮高度微调
                            .clickable { onChatClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp) // 增加内边距
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "联系",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "联系",
                                fontSize = 11.sp, // 文字微调
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4285F4)
                            )
                        }
                    }
                }
            }
        }
    }
}
