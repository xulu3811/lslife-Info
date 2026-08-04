package com.lianshan.lslife.feature.profile

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
import com.lianshan.lslife.ui.components.PostListCard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.theme.Dimens
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.xxl)
                ) {
                    // Header User Info
                    item {
                        state.user?.let { user ->
                            Surface(
                                color = scheme.surface,
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.md)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Dimens.lg),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = user.avatar,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(80.dp).clip(CircleShape).background(scheme.surfaceVariant),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.md))
                                    Text(
                                        text = user.nickname ?: "连山用户",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.xs))
                                    
                                    val authText = user.authLabel ?: "认证个人用户"
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
                                            text = "加入连山同城: $formatted",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Posts Title
                    if (state.posts.isNotEmpty()) {
                        item {
                            Text(
                                text = "TA 的发布",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.md)
                            )
                        }
                    }

                    // Posts List
                    items(state.posts) { post ->
                        Box(modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.xs)) {
                            PostListCard(
                                post = post,
                                onClick = { onOpenPost(post.id) },
                                onChatClick = { onChatClick(post.user?.id ?: "", post.user?.nickname ?: "用户") }
                            )
                        }
                    }
                    
                    if (state.posts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(Dimens.xl), contentAlignment = Alignment.Center) {
                                Text("TA 还没有发布任何内容", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
