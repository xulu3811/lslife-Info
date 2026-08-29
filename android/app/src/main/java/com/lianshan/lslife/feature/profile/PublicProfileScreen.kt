package com.qingyuan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingyuan.lslife.ui.components.PostListCard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.ui.components.ErrorBox
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.UserAvatar
import com.qingyuan.lslife.ui.theme.Dimens
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import com.qingyuan.lslife.ui.components.GridPostCard
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onChatClick: (String, String) -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadProfileAndPosts(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.user?.nickname ?: "用户主页") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    titleContentColor = scheme.onSurface
                )
            )
        },
        containerColor = scheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                LoadingBox()
            } else if (state.error != null) {
                ErrorBox(message = state.error!!, onRetry = { viewModel.loadProfileAndPosts(userId) })
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.sm),
                    contentPadding = PaddingValues(bottom = Dimens.xxl),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                    verticalItemSpacing = Dimens.sm
                ) {
                    // Header User Info
                    item(span = StaggeredGridItemSpan.FullLine) {
                        state.user?.let { user ->
                            Surface(
                                color = scheme.surface,
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.md, top = Dimens.xs)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Dimens.lg),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    UserAvatar(
                                        url = user.avatar,
                                        nickname = user.nickname,
                                        size = 80.dp
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.md))
                                    Text(
                                        text = user.nickname ?: "\u8FDE\u5C71\u7528\u6237",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.xs))
                                    
                                    val authText = user.authLabel ?: "\u8BA4\u8BC1\u4E2A\u4EBA\u7528\u6237"
                                    val authColor = if (user.isMerchant) Color(0xFFD4AF37) else scheme.onSurfaceVariant
                                    
                                    Text(
                                        text = authText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = authColor
                                    )
                                    
                                    user.createdAt?.let { dateStr ->
                                        Spacer(modifier = Modifier.height(Dimens.xs))
                                        val date = try {
                                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(dateStr)
                                        } catch (e: Exception) { null }
                                        val formatted = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(it) } ?: dateStr
                                        Text(
                                            text = "\u52A0\u5165\u8FDE\u5C71\u540C\u57CE: $formatted",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Dimens.md))
                                    
                                    val isFollowing = user.isFollowing
                                    Button(
                                        onClick = { viewModel.toggleFollow(user.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowing) Color(0xFFF5F5F5) else Color(0xFFFF2442),
                                            contentColor = if (isFollowing) Color(0xFF999999) else Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 0.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(if (isFollowing) "\u5DF2\u5173\u6CE8" else "+ \u5173\u6CE8", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.md))

                                    // 商家资质 / 营业执照区域
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                        color = if (user.isMerchant && !user.businessLicenseUrl.isNullOrEmpty()) Color(0xFFFFFDF5) else scheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (user.isMerchant && !user.businessLicenseUrl.isNullOrEmpty()) Color(0xFFE6D28C) else scheme.outlineVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (user.isMerchant) Color(0xFFD4AF37) else scheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "\u5546\u5BB6\u8D44\u8D28",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (user.isMerchant) Color(0xFF8A6D1C) else scheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Surface(
                                                    color = if (user.isMerchant) Color(0xFFFFF3CD) else scheme.surfaceVariant,
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = if (user.isMerchant) "已官方认证" else "未认证",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (user.isMerchant) Color(0xFF856404) else scheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(Dimens.xs))
                                            HorizontalDivider(
                                                color = if (user.isMerchant) Color(0xFFF0E4B8) else scheme.outlineVariant.copy(alpha = 0.3f),
                                                thickness = 0.5.dp
                                            )
                                            Spacer(modifier = Modifier.height(Dimens.xs))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "\u8425\u4E1A\u6267\u7167\u7167\u7247",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = scheme.onSurfaceVariant
                                                )

                                                if (user.isMerchant && !user.businessLicenseUrl.isNullOrEmpty()) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                                            .clickable { previewImageUrl = user.businessLicenseUrl }
                                                            .padding(4.dp)
                                                    ) {
                                                        AsyncImage(
                                                            model = user.businessLicenseUrl,
                                                            contentDescription = "营业执照照片",
                                                            modifier = Modifier
                                                                .size(width = 56.dp, height = 40.dp)
                                                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                                                .background(Color.LightGray),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "\u70B9\u51FB\u67E5\u770B",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Color(0xFF1976D2),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                } else {
                                                    Surface(
                                                        color = scheme.surfaceVariant.copy(alpha = 0.6f),
                                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "\u65E0",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = scheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Posts Title
                    if (state.posts.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = "TA \u7684\u53D1\u5E03",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = Dimens.sm, vertical = Dimens.sm)
                            )
                        }
                    }

                    // Posts List
                    items(state.posts) { post ->
                        GridPostCard(
                            post = post,
                            onClick = { onOpenPost(post.id) }
                        )
                    }
                    
                    if (state.posts.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Box(modifier = Modifier.fillMaxWidth().padding(Dimens.xl), contentAlignment = Alignment.Center) {
                                Text("TA \u8FD8\u6CA1\u6709\u53D1\u5E03\u4EFB\u4F55\u5185\u5BB9", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 营业执照全屏大图预览弹窗
        if (previewImageUrl != null) {
            Dialog(
                onDismissRequest = { previewImageUrl = null },
                properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { previewImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 顶部栏
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "商家资质 - 营业执照",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { previewImageUrl = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = Color.White
                                )
                            }
                        }

                        // 图片展示区
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = previewImageUrl,
                                contentDescription = "营业执照照片大图",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
