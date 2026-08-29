package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.ui.components.ErrorBox
import com.qingyuan.lslife.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewListScreen(
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
    viewModel: AdminReviewListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("待审核列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    titleContentColor = scheme.onSurface
                )
            )
        },
        containerColor = Color(0xFFF4F5F7)
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingBox(modifier = Modifier.padding(padding).fillMaxSize())
            }
            state.error != null -> {
                ErrorBox(
                    message = state.error!!,
                    onRetry = { viewModel.loadReviewList() },
                    modifier = Modifier.padding(padding).fillMaxSize()
                )
            }
            state.posts.isEmpty() -> {
                com.qingyuan.lslife.ui.components.EmptyState(
                    title = "暂无待审核帖子",
                    subtitle = "当前没有需要人工处理的内容",
                    modifier = Modifier.padding(padding).fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    items(state.posts) { post ->
                        AdminFeedCard(post = post, onClick = { onPostClick(post.id) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFeedCard(
    post: Post,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Publisher Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.user?.avatar,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.user?.nickname ?: "未知用户",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "提交时间: ${post.createdAt}",
                        fontSize = 12.sp,
                        color = scheme.onSurfaceVariant
                    )
                }
                
                // Status tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = scheme.errorContainer,
                    contentColor = scheme.onErrorContainer
                ) {
                    Text(
                        text = "待人工审核",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post Summary
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.description,
                fontSize = 14.sp,
                color = scheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
