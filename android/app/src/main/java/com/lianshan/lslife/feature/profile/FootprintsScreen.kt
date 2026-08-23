package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post
import com.lianshan.lslife.ui.components.SkeletonCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootprintsScreen(
    onBack: () -> Unit,
    onOpenPostDetail: (String) -> Unit,
    onOpenHome: () -> Unit = {},
    viewModel: FootprintsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空足迹", fontWeight = FontWeight.Bold) },
            text = { Text("确定要清空所有的浏览足迹吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    viewModel.clearAll()
                }) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("我的足迹", fontSize = 18.sp, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (state.posts.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "清空足迹", tint = Color.Gray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        if (state.loading && state.posts.isEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(4) {
                    SkeletonCard(modifier = Modifier.fillMaxWidth().height(116.dp))
                }
            }
        } else if (state.posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无足迹",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "你还没有浏览过任何内容，足迹最多保留50条",
                        fontSize = 12.5.sp,
                        color = Color(0xFF999999),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onOpenHome,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("去逛逛", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.posts, key = { it.id }) { post ->
                    FootprintPostCard(
                        post = post,
                        onClick = { onOpenPostDetail(post.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FootprintPostCard(
    post: Post,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val image = post.images?.firstOrNull() ?: post.merchant?.logo ?: post.user?.avatar
            if (!image.isNullOrEmpty()) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 92.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = post.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val desc = if (post.merchant != null) post.merchant.name else post.description
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val priceStr = if (post.price != null && post.price > 0) {
                        "¥${post.price.toInt()}"
                    } else if (post.price == 0.0) {
                        "免费"
                    } else {
                        "面议 / 详询"
                    }
                    Text(
                        text = priceStr,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE93323)
                    )
                }
            }
        }
    }
}
