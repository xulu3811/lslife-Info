package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.lianshan.lslife.core.model.Post

import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDynamicFeedScreen(
    posts: List<Post>,
    onPostClick: (String) -> Unit,
    onLinkedCommerceClick: (String) -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    val listState = rememberLazyStaggeredGridState()

    // Trigger load more
    androidx.compose.runtime.LaunchedEffect(listState) {
        androidx.compose.runtime.snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= posts.size - 4) {
                    onLoadMore()
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(posts, key = { it.id }) { post ->
                MomentFeedCard(
                    avatarUrl = post.user?.avatar,
                    userName = post.user?.nickname ?: post.merchant?.name ?: "连山用户",
                    title = post.title,
                    description = post.description,
                    imageUrl = post.images.firstOrNull(),
                    likeCount = post.likeCount,
                    linkedCommerce = post.linkedCommerce,
                    onCardClick = { onPostClick(post.id) },
                    onLinkedCommerceClick = { onLinkedCommerceClick(it) }
                )
            }
        }
    }
}
