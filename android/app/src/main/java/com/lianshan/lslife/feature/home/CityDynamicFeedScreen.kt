package com.qingyuan.lslife.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Phone
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDynamicsScreen(
    viewModel: DynamicsViewModel,
    onPostClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val newPostsCount by viewModel.newPostsCount.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F5F8))) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { 
                isRefreshing = true
                viewModel.loadInitialData()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(top = 8.dp, bottom = Dimens.xxl),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState is DynamicsUiState.Loading) {
                    items(6) { DynamicsSkeletonList() }
                } else if (uiState is DynamicsUiState.Error) {
                    item { Text((uiState as DynamicsUiState.Error).message, modifier = Modifier.padding(16.dp)) }
                } else if (uiState is DynamicsUiState.Success) {
                    val posts = (uiState as DynamicsUiState.Success).items
                    items(posts, key = { it.id }) { post ->
                        MomentFeedCard(
                            item = post,
                            onClick = { onPostClick(post.id) },
                            onChatClick = { onChatClick(post.id) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF3F5F8))
                    }
                    if ((uiState as DynamicsUiState.Success).hasMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }

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
                color = Color(0xFF4285F4),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable {
                    viewModel.fetchNewData()
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                }
            ) {
                Text(
                    text = "有 ${newPostsCount} 条新动态",
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
fun DynamicsSkeletonList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(Color(0xFFF3F5F8), CircleShape))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Box(modifier = Modifier.width(120.dp).height(14.dp).background(Color(0xFFF3F5F8), RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.width(80.dp).height(12.dp).background(Color(0xFFF3F5F8), RoundedCornerShape(4.dp)))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFF3F5F8), RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).background(Color(0xFFF3F5F8), RoundedCornerShape(4.dp)))
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF3F5F8))
}

@Composable
fun MomentFeedCard(
    item: Post,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val context = LocalContext.current
    val baseUrl = com.qingyuan.lslife.BuildConfig.API_BASE_URL
    val avatarUrl = item.user?.avatar ?: ""
    val absoluteAvatarUrl = if (avatarUrl.startsWith("http") || avatarUrl.startsWith("android.resource://") || avatarUrl.startsWith("file://")) {
        avatarUrl
    } else if (avatarUrl.isNotEmpty()) {
        "${baseUrl.removeSuffix("/")}${if (avatarUrl.startsWith("/")) "" else "/"}$avatarUrl"
    } else {
        ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header: Avatar, Name, Time
        Row(verticalAlignment = Alignment.CenterVertically) {
            com.qingyuan.lslife.ui.components.GoogleAvatar(
                url = absoluteAvatarUrl,
                size = 36.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.user?.nickname ?: "清远用户",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.createdAt.take(16).replace("T", " "), // Simple formatting
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Text
        val displayText = if (item.title.isNotBlank()) item.title else item.description
        if (displayText.isNotBlank()) {
            Text(
                text = displayText,
                fontSize = 14.sp,
                color = Color(0xFF1F2937),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Images Grid
        if (item.images.isNotEmpty()) {
            MomentImageGrid(images = item.images, baseUrl = baseUrl)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Location & Actions
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (!item.city.isNullOrEmpty() || !item.district.isNullOrEmpty()) {
                val locText = listOfNotNull(item.city, item.district).joinToString("·")
                Text(
                    text = locText,
                    fontSize = 11.sp,
                    color = Color(0xFF4285F4), // Primary Blue
                    modifier = Modifier
                        .background(Color(0xFFF0F4F9), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            if (!item.contactPhone.isNullOrEmpty()) {
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${item.contactPhone}") }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0F4F9),
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Filled.Phone, contentDescription = "拨打", tint = Color(0xFF4285F4), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("拨打", fontSize = 11.sp, color = Color(0xFF4285F4), fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Surface(
                    onClick = { onChatClick() },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0F4F9),
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "私聊", tint = Color(0xFF4285F4), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("私聊", fontSize = 11.sp, color = Color(0xFF4285F4), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun MomentImageGrid(images: List<String>, baseUrl: String) {
    val maxImages = images.take(9)
    val context = LocalContext.current
    
    if (maxImages.size == 1) {
        val img = maxImages.first()
        val absoluteUrl = if (img.startsWith("http") || img.startsWith("android.resource://") || img.startsWith("file://")) img else "${baseUrl.removeSuffix("/")}${if (img.startsWith("/")) "" else "/"}$img"
        AsyncImage(
            model = ImageRequest.Builder(context).data(absoluteUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f, matchHeightConstraintsFirst = false) // 3:2 比例，与商品卡片一致
                .clip(RoundedCornerShape(12.dp)) // 更平滑的大圆角
                .background(Color(0xFFF3F5F8))
        )
    } else {
        val columns = if (maxImages.size == 4) 2 else 3
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            maxImages.chunked(columns).forEach { rowImages ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rowImages.forEach { img ->
                        val absoluteUrl = if (img.startsWith("http") || img.startsWith("android.resource://") || img.startsWith("file://")) img else "${baseUrl.removeSuffix("/")}${if (img.startsWith("/")) "" else "/"}$img"
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(absoluteUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3F5F8))
                        )
                    }
                    if (rowImages.size < columns) {
                        repeat(columns - rowImages.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
