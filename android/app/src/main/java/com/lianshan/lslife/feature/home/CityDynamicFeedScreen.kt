package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDynamicFeedScreen(
    posts: List<Post>,
    onPostClick: (String) -> Unit,
    onLinkedCommerceClick: (String) -> Unit, // kept for signature compatibility
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    val gridState = rememberLazyGridState()

    // Trigger load more
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                DealItemCard(
                    post = post,
                    onClick = { onPostClick(post.id) }
                )
            }
        }
    }
}

@Composable
fun DealItemCard(
    post: Post,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. 顶部主图 (The Hero Image)
            val imageUrl = post.images.firstOrNull() ?: ""
            val baseUrl = com.lianshan.lslife.BuildConfig.API_BASE_URL
            val absoluteUrl = if (imageUrl.startsWith("http") || imageUrl.startsWith("android.resource://") || imageUrl.startsWith("file://")) {
                imageUrl
            } else {
                "${baseUrl.removeSuffix("/")}${if (imageUrl.startsWith("/")) "" else "/"}$imageUrl"
            }

            AsyncImage(
                model = absoluteUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 精简标题 (The Title)
            Text(
                text = post.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 价格与标签行 (Price & Tags)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "¥",
                    fontSize = 12.sp,
                    color = Color(0xFFFF4142),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "${post.price ?: 0.0}",
                    fontSize = 16.sp,
                    color = Color(0xFFFF4142),
                    fontWeight = FontWeight.Bold,
                )
                
                Spacer(modifier = Modifier.width(6.dp))

                // Tag
                Box(
                    modifier = Modifier
                        .background(color = Color(0xFFFF4142), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "特价",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
