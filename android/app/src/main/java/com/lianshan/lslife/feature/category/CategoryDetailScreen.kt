package com.qingyuan.lslife.feature.category

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.CategoryNode
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.ui.components.EmptyState
import com.qingyuan.lslife.ui.components.InfoPublishCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
    onChatClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()
    val context = LocalContext.current
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    val isServiceCategory = categoryId in listOf("cat_2_service", "cat_3_repair", "cat_7_carpool", "cat_8_job", "cat_9_life", "cat_10_edu")

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

    Scaffold(
        topBar = {
            ModernCategoryHeader(
                categoryName = state.category?.name ?: "全部分类",
                subCategories = state.subCategories,
                selectedSubCategoryId = state.selectedSubCategory ?: "all",
                onSubCategorySelect = viewModel::onSubCategory,
                onBack = onBack,
                onSearchClick = {
                    Toast.makeText(context, "搜索 ${state.category?.name ?: "当前分类"}...", Toast.LENGTH_SHORT).show()
                },
                onFilterClick = {
                    showFilterBottomSheet = true
                }
            )
        },
        containerColor = Color(0xFFF3F5F8)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


            // Feed Area
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (!state.loading && state.posts.isEmpty()) {
                    EmptyState(title = "暂无内容", subtitle = "该分类下暂无发布内容", modifier = Modifier.fillMaxSize())
                } else {
                    // 统一使用双列瀑布流 (Style A) 以增加曝光数量
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            InfoPublishCard(
                                post = post,
                                onClick = { onPostClick(post.id) },
                                onPhoneClick = {
                                    val phone = post.contactPhone
                                    if (phone.isNullOrBlank()) {
                                        Toast.makeText(context, "该发布者未留电话", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:$phone")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "无法打开拨号键盘", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onChatClick = {
                                    val targetId = post.user?.id
                                    val targetName = post.user?.nickname ?: "发布者"
                                    if (targetId.isNullOrBlank()) {
                                        Toast.makeText(context, "无法获取用户信息", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onChatClick(targetId, targetName, post.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter BottomSheet
    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FilterBottomSheetContent(
                isServiceCategory = isServiceCategory,
                onDismiss = { showFilterBottomSheet = false }
            )
        }
    }
}

/**
 * 欧化现代极简风三合一 Header (ModernCategoryHeader)
 * 结合 Uber Eats / Airbnb 极简设计与 Joybuy 高端 3D 悬浮质感
 */
@Composable
fun ModernCategoryHeader(
    categoryName: String,
    subCategories: List<CategoryNode>,
    selectedSubCategoryId: String,
    onSubCategorySelect: (String) -> Unit,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 10.dp)
        ) {
            // 模块 1：三合一综合导航栏 (Unified Search & Filter Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：返回按键
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 右侧主体：药丸形三合一组件
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF3F5F8),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { onSearchClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 搜索图标 + 融合标题动态占位符
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "搜索 $categoryName...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 内嵌圆角筛选图标按钮
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onFilterClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "筛选",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 模块 2：3D 扁平化 (Soft UI) 悬浮分类卡片 (Floating Category Chips)
            val displaySubs = remember(subCategories) {
                listOf(CategoryNode(id = "all", name = "全部", icon = "all")) + subCategories
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(displaySubs, key = { it.id }) { subCat ->
                    val isSelected = selectedSubCategoryId == subCat.id

                    if (isSelected) {
                        // 选中态：Google M3 风格 (浅蓝底色 + 主题蓝文字，无阴影)
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F0FE))
                                .clickable { onSubCategorySelect(subCat.id) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subCat.name,
                                color = Color(0xFF1A73E8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // 未选态：纯粹极简文字 + 浅色交互
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(CircleShape)
                                .clickable { onSubCategorySelect(subCat.id) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subCat.name,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))
        }
    }
}

/** 筛选 BottomSheet 弹出抽屉 */
@Composable
private fun FilterBottomSheetContent(
    isServiceCategory: Boolean,
    onDismiss: () -> Unit
) {
    var selectedSort by remember { mutableStateOf("智能排序") }
    var selectedArea by remember { mutableStateOf("全城区域") }

    val sortOptions = if (isServiceCategory) {
        listOf("智能排序", "好评优先", "距离最近", "响应最快")
    } else {
        listOf("智能排序", "价格从低到高", "价格从高到低", "最新发布")
    }
    val areaOptions = listOf("全城区域", "清远市城", "吉田镇", "太保镇", "禾洞镇", "福堂镇", "小三江镇", "加益镇")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("全城筛选与排序", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("区域范围", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OptGrid(options = areaOptions, selected = selectedArea, onSelect = { selectedArea = it })
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("排序规则", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OptGrid(options = sortOptions, selected = selectedSort, onSelect = { selectedSort = it })
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = CircleShape
        ) {
            Text("确认筛选", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptGrid(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val isSel = option == selected
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSel) Color(0xFFE8F0FE) else Color(0xFFF3F5F8))
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = option,
                    fontSize = 12.sp,
                    color = if (isSel) Color(0xFF1A73E8) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ServiceListFeedCard(
    post: Post,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Avatar / Logo with larger size and better aspect
            val imageUrl = post.images.firstOrNull() ?: post.user?.avatar
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
            )
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Middle: Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                        Text("上门快", color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                        Text("资质认证", color = Color(0xFFFA8C16), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "距您 ${(1..5).random()}.${(1..9).random()}km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Right: IM & Call Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onChat,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape) // Light red/orange tint for chat
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "私聊",
                        tint = Color(0xFFFF5000),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "呼叫",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

