package com.lianshan.lslife.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.lianshan.lslife.core.model.Post
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDynamicsScreen(
    viewModel: DynamicsViewModel,
    onPostClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val newPostsCount by viewModel.newPostsCount.collectAsState()
    val gridState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    // Trigger load more
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (uiState is DynamicsUiState.Success) {
                    val postsSize = (uiState as DynamicsUiState.Success).items.size
                    if (lastIndex != null && lastIndex >= postsSize - 4) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    LaunchedEffect(uiState) {
        if (uiState !is DynamicsUiState.Loading) {
            isRefreshing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadInitialData()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is DynamicsUiState.Loading -> {
                    if (!isRefreshing) {
                        DynamicsSkeletonGrid()
                    }
                }
                is DynamicsUiState.Success -> {
                    val feedItems = state.items
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(feedItems, key = { it.id }) { item ->
                            DynamicFeedCard(
                                item = item,
                                onClick = { onPostClick(item.id) },
                                onChatClick = { onChatClick(item.id) }
                            )
                        }
                    }
                }
                is DynamicsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadInitialData() }) {
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }

        // Floating Pill UX for new items
        AnimatedVisibility(
            visible = newPostsCount > 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFF4D4F),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable {
                    viewModel.fetchNewData()
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                    }
                }
            ) {
                Text(
                    text = "👇 有 $newPostsCount 条新动态",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DynamicsSkeletonGrid() {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) { index ->
            val heightInfo = if (index % 2 == 0) 220.dp else 180.dp
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightInfo)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFFEEEEEE))
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(14.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).background(Color(0xFFEEEEEE), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(12.dp)
                                    .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicFeedCard(
    item: Post,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            val imageUrl = item.images.firstOrNull() ?: ""
            val baseUrl = com.lianshan.lslife.BuildConfig.API_BASE_URL
            val absoluteUrl = if (imageUrl.startsWith("http") || imageUrl.startsWith("android.resource://") || imageUrl.startsWith("file://")) {
                imageUrl
            } else if (imageUrl.isNotEmpty()) {
                "${baseUrl.removeSuffix("/")}${if (imageUrl.startsWith("/")) "" else "/"}$imageUrl"
            } else {
                ""
            }

            val aspectRatio = item.imageWidth?.let { w ->
                item.imageHeight?.let { h ->
                    if (h > 0) w.toFloat() / h.toFloat() else 1f
                }
            } ?: 1f // fallback
            
            val displayText = if (item.title.isNotBlank()) item.title else item.description

            if (absoluteUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(absoluteUrl)
                        .crossfade(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.coerceIn(0.5f, 1.5f))
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(Color(0xFFEEEEEE))
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (displayText.isNotBlank()) {
                    Text(
                        text = displayText,
                        fontSize = 13.sp,
                        color = Color(0xFF222222),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                val gradientColors = listOf(
                    listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
                    listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
                    listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
                    listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                    listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                )
                val colorIndex = kotlin.math.abs(item.id.hashCode()) % gradientColors.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(gradientColors[colorIndex]))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                val avatarUrl = item.user?.avatar ?: ""
                val authorName = item.user?.nickname ?: "匿名用户"
                
                val absoluteAvatarUrl = if (avatarUrl.startsWith("http") || avatarUrl.startsWith("android.resource://") || avatarUrl.startsWith("file://")) {
                    avatarUrl
                } else if (avatarUrl.isNotEmpty()) {
                    "${baseUrl.removeSuffix("/")}${if (avatarUrl.startsWith("/")) "" else "/"}$avatarUrl"
                } else {
                    ""
                }

                AsyncImage(
                    model = absoluteAvatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE))
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = authorName,
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (!item.distanceText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = item.distanceText,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Action button at bottom right (phone dial or chat)
                if (!item.contactPhone.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "拨打",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${item.contactPhone}")
                                }
                                context.startActivity(intent)
                            }
                            .padding(4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "聊天",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onChatClick() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
