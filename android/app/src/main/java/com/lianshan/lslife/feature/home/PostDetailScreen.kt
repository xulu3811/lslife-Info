package com.lianshan.lslife.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCartCheckout
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onChatClick: (String, String) -> Unit, // targetUserId, targetName
    onBuyClick: (String) -> Unit, // postId
    onPhoneClick: (String) -> Unit = {}, // phone number
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商品详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    titleContentColor = scheme.onSurface
                )
            )
        },
        bottomBar = {
            if (state.post != null) {
                Surface(
                    color = scheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = Dimens.lg, vertical = Dimens.sm)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!com.lianshan.lslife.core.config.AppConfig.ENABLE_COMMERCE_CART || state.post?.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH || state.post?.tradeMode == com.lianshan.lslife.core.model.TradeMode.INFO) {
                            // Light Info/IM Contact Mode: 收藏, 在线私聊, 拨打电话
                            OutlinedButton(
                                onClick = { /* TODO: 收藏功能 */ },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Filled.FavoriteBorder, null, modifier = Modifier.size(18.dp))
                            }
                            Button(
                                onClick = {
                                    val targetId = state.post?.user?.id
                                    if (targetId != null) {
                                        onChatClick(targetId, state.post?.user?.nickname ?: "发布者")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5000)),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Filled.Chat, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("私聊/联系TA", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                            Button(
                                onClick = {
                                    val phone = state.post?.contactPhone
                                    if (!phone.isNullOrBlank()) {
                                        onPhoneClick(phone)
                                    } else {
                                        android.widget.Toast.makeText(context, "暂无电话联系方式", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160)),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Filled.Phone, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("拨打电话", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                        } else {
                            // COMMERCE BottomBar: 联系卖家, 加入购物车
                            OutlinedButton(
                                onClick = {
                                    val targetId = state.post?.user?.id
                                    if (targetId != null) {
                                        onChatClick(targetId, state.post?.user?.nickname ?: "卖家")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Filled.Chat, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("联系卖家", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                            
                            Button(
                                onClick = { 
                                    viewModel.addToCart(
                                        onSuccess = { onBuyClick(state.post!!.id) },
                                        onError = { msg -> android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.error),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Filled.ShoppingCartCheckout, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("加入购物车", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF7F8FA) // Slightly gray background
    ) { padding ->
        when {
            state.loading && state.post == null -> {
                LoadingBox(modifier = Modifier.padding(padding).fillMaxSize())
            }
            state.error != null -> {
                ErrorBox(message = state.error!!, onRetry = { viewModel.loadPost(postId) }, modifier = Modifier.padding(padding))
            }
            state.post != null -> {
                val post = state.post!!
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize()
                ) {
                    // Images Carousel
                    if (post.images.isNotEmpty()) {
                        item {
                            val pagerState = rememberPagerState(pageCount = { post.images.size })
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                    AsyncImage(
                                        model = post.images[page],
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Page Indicator
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = Dimens.sm),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(post.images.size) { index ->
                                        val color = if (pagerState.currentPage == index) scheme.primary else Color.White.copy(alpha = 0.5f)
                                        Box(
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Price and Title Card
                    item {
                        Surface(color = scheme.surface, modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.sm)) {
                            Column(modifier = Modifier.padding(Dimens.lg)) {
                                if (post.price != null && post.price > 0) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("¥", style = MaterialTheme.typography.titleMedium, color = scheme.error, fontWeight = FontWeight.Bold)
                                        Text("${post.price}", style = MaterialTheme.typography.headlineLarge, color = scheme.error, fontWeight = FontWeight.ExtraBold)
                                    }
                                } else {
                                    Text("面议", style = MaterialTheme.typography.headlineMedium, color = scheme.error, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(modifier = Modifier.height(Dimens.sm))
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(Dimens.xs))
                                Text(
                                    text = post.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = scheme.onSurfaceVariant,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }

                    // Attributes Card
                    if (post.attributes.isNotEmpty()) {
                        item {
                            Surface(color = scheme.surface, modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.sm)) {
                                Column(modifier = Modifier.padding(Dimens.lg)) {
                                    Text("商品参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = Dimens.sm))
                                    post.attributes.forEach { (key, element) ->
                                        val valueStr = if (element is kotlinx.serialization.json.JsonPrimitive) element.content else element.toString()
                                        if (valueStr.isNotBlank()) {
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Text(
                                                    text = when(key) {
                                                        "brand" -> "品牌"
                                                        "condition" -> "成色"
                                                        "purchaseDate" -> "购买时间"
                                                        "shipping" -> "邮费"
                                                        "parameters" -> "详细参数"
                                                        else -> key
                                                    },
                                                    modifier = Modifier.width(80.dp),
                                                    color = scheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = valueStr,
                                                    modifier = Modifier.weight(1f),
                                                    color = scheme.onSurface,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Publisher Card
                    item {
                        Surface(color = scheme.surface, modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.lg)) {
                            Row(
                                modifier = Modifier.padding(Dimens.lg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = post.user?.avatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(scheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(Dimens.md))
                                Column {
                                    Text(post.user?.nickname ?: "连山用户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("认证个人用户", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowBack, null, tint = Color.Transparent) // Placeholder for layout balance or 'view profile' icon
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(Dimens.xxl))
                    }
                }
            }
        }
    }
}
