package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ShoppingBag
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
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onOpenPostDetail: (String) -> Unit,
    onOpenHome: () -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "我的收藏", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        if (state.loading && state.posts.isEmpty()) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        if (state.posts.isEmpty()) {
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
                        Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无收藏的商品或服务",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "在浏览同城好物或便民服务时，点击收藏即可在这里快速找到",
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
                        Text("去首页逛逛", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.posts, key = { it.id }) { post ->
                FavoritePostCard(
                    post = post,
                    onClick = { onOpenPostDetail(post.id) },
                    onRemoveFavorite = { viewModel.removeFavorite(post.id) }
                )
            }
        }
    }
}

@Composable
private fun FavoritePostCard(
    post: Post,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 左侧主图
                val firstImage = post.images.firstOrNull()
                if (!firstImage.isNullOrBlank()) {
                    AsyncImage(
                        model = firstImage,
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F6F8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // 右侧信息
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 92.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // 标题
                        Text(
                            text = post.title.ifBlank { "同城信息" },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF222222),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 描述
                        if (post.description.isNotBlank()) {
                            Text(
                                text = post.description,
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // 底部：价格与位置
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 价格
                        if (post.price != null && post.price > 0) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "¥",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF3B30),
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                                Text(
                                    text = if (post.price % 1.0 == 0.0) post.price.toLong().toString() else post.price.toString(),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF3B30)
                                )
                            }
                        } else {
                            Text(
                                "面议 / 免费",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF52C41A)
                            )
                        }

                        // 取消收藏按钮
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF0F0))
                                .clickable { onRemoveFavorite() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "取消收藏",
                                tint = Color(0xFFFF4D4F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "已收藏",
                                fontSize = 11.sp,
                                color = Color(0xFFFF4D4F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 底部细分割线与附加属性/位置信息
            val displayLocation = post.town ?: post.city
            if (!displayLocation.isNullOrBlank() || post.user?.authLabel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF5F5F5))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!displayLocation.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = displayLocation,
                                fontSize = 11.sp,
                                color = Color(0xFF999999),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    val publisher = post.user?.nickname ?: "同城邻友"
                    Text(
                        text = "发布者: $publisher",
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}
