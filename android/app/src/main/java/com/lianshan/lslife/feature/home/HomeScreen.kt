package com.lianshan.lslife.feature.home

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
import com.lianshan.lslife.core.model.Banner
import com.lianshan.lslife.core.model.Post
import com.lianshan.lslife.feature.search.AdvancedFilterBottomSheet
import com.lianshan.lslife.ui.SessionViewModel
import com.lianshan.lslife.ui.components.*
import com.lianshan.lslife.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class CategoryBadgeItem(
    val id: String,
    val name: String,
    val iconUrl: String,
    val badge: String? = null
)

private val page1Categories = listOf(
    CategoryBadgeItem("cat_2_service", "家政/护理", "android.resource://com.lianshan.lslife/drawable/ic_category_service"),
    CategoryBadgeItem("cat_3_repair", "便民维修", "android.resource://com.lianshan.lslife/drawable/ic_category_repair"),
    CategoryBadgeItem("cat_4_fresh", "同城生鲜", "android.resource://com.lianshan.lslife/drawable/ic_category_fresh"),
    CategoryBadgeItem("cat_5_rent", "房屋出租", "android.resource://com.lianshan.lslife/drawable/ic_category_rent"),
    CategoryBadgeItem("cat_6_sale", "二手房产", "android.resource://com.lianshan.lslife/drawable/ic_category_sale"),
    CategoryBadgeItem("cat_7_carpool", "拼车/租车", "android.resource://com.lianshan.lslife/drawable/ic_category_carpool"),
    CategoryBadgeItem("cat_8_job", "招聘求职", "android.resource://com.lianshan.lslife/drawable/ic_category_job"),
    CategoryBadgeItem("cat_9_life", "吃喝玩乐", "android.resource://com.lianshan.lslife/drawable/ic_category_life"),
    CategoryBadgeItem("cat_10_edu", "教育培训", "android.resource://com.lianshan.lslife/drawable/ic_category_edu"),
    CategoryBadgeItem("cat_1_idle", "个人闲置", "android.resource://com.lianshan.lslife/drawable/ic_category_idle"),
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
            .background(Color(0xFFF7F7F7))
            .statusBarsPadding()
    ) {
        // 第一层：顶部搜索与本地化头栏
        TopSearchHeaderBar(
            locationText = state.currentLocation,
            hotword = state.searchHotwords.getOrElse(hotwordIndex) { "搜索本地商户、商品、服务" },
            unreadCount = unreadCount,
            onLocationClick = { Toast.makeText(context, "当前定位：连山壮族瑶族自治县", Toast.LENGTH_SHORT).show() },
            onSearchClick = onSearchClick,
            onMessageClick = onMessageClick
        )

        val tabs = listOf("首页", "同城动态")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val coroutineScope = rememberCoroutineScope()

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.White,
            contentColor = Color(0xFFFF4D4F),
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
                                color = if (isSelected) Color(0xFF111111) else Color(0xFF666666) // 平滑的灰度过渡
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
                                        .background(Color(0xFFFF4D4F), CircleShape)
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
                                            Text("—— 连山同城，贴心服务 ——", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(
                text = locationText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
        }

        // 搜索框 + 热词轮播
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF2F2F2)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
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
                        color = Color.Gray,
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
                tint = Color(0xFF333333),
                modifier = Modifier.size(26.dp)
            )
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .background(Color(0xFFFF4D4F), CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = Color.White,
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
            .background(Color(0xFFEFEFEF))
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
                        color = Color.White,
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
                        .background(if (active) Color(0xFFFF4D4F) else Color.White.copy(alpha = 0.7f))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
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
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            row2.forEach { cat ->
                KingkongItemView(cat, modifier = Modifier.weight(1f)) { id ->
                    onCategoryClick(id)
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
            .padding(vertical = 4.dp)
            .clickable { onClick(cat.id) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 彻底移除生硬的圆形白底与边框，让3D实物图标自然悬浮，极大增强呼吸感与空间感
        AsyncImage(
            model = cat.iconUrl,
            contentDescription = cat.name,
            modifier = Modifier.size(48.dp) // 统一基准：48.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cat.name,
            fontSize = 11.sp, // 与分类页协调
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium,
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

    Column(modifier = Modifier.fillMaxWidth()) {
        // 移除多余的视觉留白，提升空间利用率 (原本这里有 18dp Spacer)

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color(0x14000000), // 柔和的浅色底层阴影，扩散自然
                    ambientColor = Color(0x0A000000)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp), // 减少内边距缩小高度
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
                                fontSize = if (selected) 14.sp else 12.sp, // 缩小字体
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color(0xFF111111) else Color.Gray
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(16.dp, 3.dp)
                                        .background(Color(0xFFFF4D4F), CircleShape)
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
                    Text("筛选", fontSize = 12.sp, color = Color.Gray) // 缩小字体
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filter",
                        tint = Color.Gray,
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
        color = if (post.isUrgent) Color(0xFFFFF7F7) else Color.White,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x08000000)
            )
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            val imageUrl = post.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f, matchHeightConstraintsFirst = false) // 限制为 3:2 比例，显著降低图片高度
                        .background(Color(0xFFF7F7F7)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl + "?x-oss-process=image/resize,w_500",
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop, // 确保图片裁剪不缩放变形
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = post.title ?: "同城发布",
                    fontSize = 13.sp, // 缩小至13.sp
                    fontWeight = FontWeight.Bold, // 保持粗体
                    color = Color(0xFF111111),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp)) // 从10.dp缩小至8.dp，内边距更紧凑

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarUrl = post.user?.avatar ?: ""
                    val authorName = post.user?.nickname ?: "连山用户"
                    
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(20.dp) // 头像限制在20.dp
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = authorName,
                        fontSize = 11.sp, // 发布者昵称 11.sp
                        color = Color(0xFF666666),
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
                            Text(text = "金牌", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (post.isUrgent) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE53935), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "急售", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier
                            .height(24.dp) // 按钮高度更迷你
                            .clickable { onChatClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp) // 缩小内边距
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "联系",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(11.dp) // Icon 缩小
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "联系",
                                fontSize = 10.sp, // 文字缩小至 10.sp
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
