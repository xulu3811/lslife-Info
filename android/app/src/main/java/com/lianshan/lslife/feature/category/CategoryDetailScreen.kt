package com.lianshan.lslife.feature.category

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.TradeMode
import com.lianshan.lslife.ui.components.InfoPublishCard
import com.lianshan.lslife.ui.components.O2OProductCard
import com.lianshan.lslife.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()
    val context = LocalContext.current

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
            CenterAlignedTopAppBar(
                title = { Text(text = state.category?.name ?: "分类", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F6F8)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sub-categories capsule tags
            if (state.subCategories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displaySubs = listOf(com.lianshan.lslife.core.model.CategoryNode(id = "all", name = "全部", icon = "all")) + state.subCategories
                    items(displaySubs) { subCat ->
                        val selected = state.selectedSubCategory == subCat.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) Color(0xFFE8F5E9) else Color(0xFFF0F0F0),
                            modifier = Modifier.clickable { viewModel.onSubCategory(subCat.id) }
                        ) {
                            Text(
                                text = subCat.name,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color(0xFF4CAF50) else Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (!state.loading && state.posts.isEmpty()) {
                    EmptyState(
                        title = "暂无内容",
                        subtitle = "该分类下暂无发布内容",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            if (post.tradeMode == TradeMode.INFO_PUBLISH || post.tradeMode == TradeMode.INFO) {
                                InfoPublishCard(
                                    post = post,
                                    onClick = { onPostClick(post.id) },
                                    onPhoneClick = { 
                                        if (post.contactPhone.isNullOrBlank()) {
                                            Toast.makeText(context, "发布者未留电话", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "拨打电话: ${post.contactPhone}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onChatClick = {
                                        Toast.makeText(context, "联系发布者私聊", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                O2OProductCard(
                                    post = post, 
                                    onClick = { onPostClick(post.id) },
                                    onAddCartClick = { 
                                        Toast.makeText(context, "加入购物车功能开发中", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                        if (state.loadingMore) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
