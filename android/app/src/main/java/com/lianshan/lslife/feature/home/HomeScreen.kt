package com.lianshan.lslife.feature.home

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(start = Dimens.lg, end = Dimens.lg, top = 4.dp, bottom = Dimens.xl),
                horizontalArrangement = Arrangement.spacedBy(Dimens.listGap),
                verticalItemSpacing = Dimens.listGap,
                modifier = Modifier.fillMaxSize(),
            ) {
                // 第二层：头部大 Banner 轮播 (广告位)
                if (state.banners.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        HomeBannerCarousel(banners = state.banners, onBannerClick = { Toast.makeText(context, it.title, Toast.LENGTH_SHORT).show() })
                    }
                }

                // 第三层：金刚区导航 (单页 6 个品类发现大图)
                item(span = StaggeredGridItemSpan.FullLine) {
                    KingkongCategoryPager(
                        onCategoryClick = onNavigateToCategory,
                        onNavigateToCategoryTab = onNavigateToCategoryTab
                    )
                }

                // 第四层：场景化展位矩阵 (为您甄选：同城严选/品牌好店 + 限时福利/特惠抢单)
                // 即使后端没有数据，也强制展示占位区块，让商家看到展位价值
                item(span = StaggeredGridItemSpan.FullLine) {
                    HomeShowcaseMatrix(
                        matrixData = state.matrixData,
                        onPostClick = onOpenPost
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

/** 第三层：金刚区导航 (4列 x 2行 横向 Pager 翻页) */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KingkongCategoryPager(
    onCategoryClick: (String) -> Unit,
    onNavigateToCategoryTab: () -> Unit
) {
    val allCategories = (page1Categories + page2Categories).take(10)
    val pages = remember(allCategories) { allCategories.chunked(8) }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 强制设置金刚区 Pager 的固定高度 (包含 2 行图标 + 间距)
        // 从而消除从 Tab1 (两行) 滑动到 Tab2 (单行) 时的底部卡片上移抖动现象
        val fixedPagerHeight = 220.dp
        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .height(fixedPagerHeight)
        ) { page ->
            val pageItems = pages[page]
            Column(modifier = Modifier.fillMaxWidth()) {
                val row1 = pageItems.take(4)
                val row2 = pageItems.drop(4).take(4)

                Row(modifier = Modifier.fillMaxWidth()) {
                    row1.forEach { cat ->
                        KingkongItemView(cat, onCategoryClick)
                    }
                    repeat(4 - row1.size) { Spacer(modifier = Modifier.weight(1f)) }
                }

                if (row2.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row2.forEach { cat ->
                            KingkongItemView(cat, onCategoryClick)
                        }
                        repeat(4 - row2.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
        
        // Pager indicator
        if (pages.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) Color(0xFFFF4D4F) else Color(0xFFE0E0E0)
                    val width = if (isSelected) 14.dp else 5.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(color)
                            .height(5.dp)
                            .width(width)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.KingkongItemView(
    cat: CategoryBadgeItem,
    onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 96.dp)
            .clickable { onClick(cat.id) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(0.5.dp, Color(0xFFEEEEEE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = cat.iconUrl,
                contentDescription = cat.name,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cat.name,
            fontSize = 13.sp,
            color = Color(0xFF333333),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 第四层：场景化展位矩阵 (为您甄选魔方) */
@Composable
private fun HomeShowcaseMatrix(
    matrixData: com.lianshan.lslife.core.model.HomeMatrixData,
    onPostClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Block 1: 畅销榜单 (隐藏)
        /*
        ShowcaseHorizontalBlock(
            title = "畅销榜单",
            subtitle = "大家都在看",
            items = matrixData.featuredMerchants,
            titleColor = Color(0xFF111111),
            onPostClick = onPostClick
        )
        */

        // Block 2: 优选好物
        ShowcaseHorizontalBlock(
            title = "优选好物",
            subtitle = "同城极速达",
            items = matrixData.specialOffers,
            titleColor = Color(0xFF111111),
            onPostClick = onPostClick
        )
    }
}

@Composable
private fun ShowcaseHorizontalBlock(
    title: String,
    subtitle: String,
    items: List<com.lianshan.lslife.core.model.Post>,
    titleColor: Color,
    onPostClick: (String) -> Unit
) {
    if (items.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(items.size) { index ->
                    val post = items[index]
                    val imageUrl = post.images.firstOrNull() ?: ""

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .width(108.dp)
                            .clickable { onPostClick(post.id) }
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = post.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = post.title ?: "同城发布",
                            color = Color(0xFF333333),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.heightIn(min = 34.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val avatarUrl = post.user?.avatar ?: ""
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val phone = post.contactPhone
                                            if (phone.isNullOrBlank()) {
                                                Toast.makeText(context, "对方暂未公开电话", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "正在拨打电话: $phone", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Call,
                                            contentDescription = "Call",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFEBEE),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onPostClick(post.id) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Chat",
                                            tint = Color(0xFFF44336),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                        fontSize = if (selected) 15.sp else 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color(0xFF111111) else Color.Gray
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
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
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("筛选", fontSize = 13.sp, color = Color.Gray)
            Icon(imageVector = Icons.Filled.FilterList, contentDescription = "Filter", tint = Color.Gray, modifier = Modifier.size(16.dp))
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
                        .height(150.dp)
                        .background(Color(0xFFF7F7F7)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl + "?x-oss-process=image/resize,w_500",
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = post.title ?: "同城发布",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 17.sp,
                    color = Color(0xFF111111),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = authorName,
                        fontSize = 11.sp,
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

                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier
                            .height(26.dp)
                            .clickable { onChatClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "联系",
                                tint = Color(0xFFFF4D4F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "联系",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFF4D4F)
                            )
                        }
                    }
                }
            }
        }
    }
}
