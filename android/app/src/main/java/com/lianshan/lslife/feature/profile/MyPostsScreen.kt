package com.qingyuan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.NetworkImage
import com.qingyuan.lslife.ui.components.SoftCard
import com.qingyuan.lslife.ui.components.StatusChip
import com.qingyuan.lslife.ui.components.StatusTone
import com.qingyuan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    onBack: () -> Unit,
    onEditPost: (String) -> Unit,
    viewModel: MyPostsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

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
                title = { Text("我的发布", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF1F2937)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = androidx.compose.ui.graphics.Color(0xFF1F2937))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8)
    ) { padding ->
        if (state.loading && state.posts.isEmpty()) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }
        
        if (state.posts.isEmpty()) {
            com.qingyuan.lslife.ui.components.EmptyState(
                title = "暂无发布内容",
                modifier = Modifier.padding(padding).fillMaxSize()
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            items(state.posts) { post ->
                MyPostCard(
                    post = post,
                    onEdit = { onEditPost(post.id) },
                    onDelist = { viewModel.updateStatus(post.id, "removed") },
                    onRelist = { viewModel.updateStatus(post.id, "pending_review") },
                    onDelete = { viewModel.deletePost(post.id) }
                )
            }
        }
    }
}

@Composable
private fun MyPostCard(
    post: Post,
    onEdit: () -> Unit,
    onDelist: () -> Unit,
    onRelist: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val statusText = when (post.status) {
        "published", "PUBLISHED" -> "已发布"
        "pending_review", "AI_REVIEWING", "MANUAL_REVIEWING" -> "审核中"
        "rejected", "REJECTED" -> "已驳回"
        "removed", "REMOVED" -> "已下架"
        else -> post.status
    }
    val statusTone = when (post.status) {
        "published", "PUBLISHED" -> StatusTone.Success
        "pending_review", "AI_REVIEWING", "MANUAL_REVIEWING" -> StatusTone.Warning
        "rejected", "REJECTED" -> StatusTone.Error
        "removed", "REMOVED" -> StatusTone.Neutral
        else -> StatusTone.Neutral
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = androidx.compose.ui.graphics.Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(Dimens.md).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                NetworkImage(
                    url = post.images.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.medium)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(statusText, statusTone)
                    }
                    Text(
                        post.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    if (post.price != null && post.price > 0) {
                        Text("¥${post.price}", style = MaterialTheme.typography.titleSmall, color = scheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (post.status == "rejected" && !post.reviewNote.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxWidth().background(scheme.errorContainer.copy(alpha = 0.5f), MaterialTheme.shapes.small).padding(Dimens.sm)) {
                    Text("驳回原因: ${post.reviewNote}", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer)
                }
            }

            HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFFF3F4F6))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                val GoogleBlue = androidx.compose.ui.graphics.Color(0xFF1A73E8)
                val GoogleRed = androidx.compose.ui.graphics.Color(0xFFEA4335)
                val GoogleGrey = androidx.compose.ui.graphics.Color(0xFF5F6368)

                TextButton(onClick = onEdit, colors = ButtonDefaults.textButtonColors(contentColor = GoogleBlue)) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("修改")
                }
                
                if (post.status == "published" || post.status == "PUBLISHED" || post.status == "pending_review" || post.status == "MANUAL_REVIEWING" || post.status == "AI_REVIEWING") {
                    TextButton(onClick = onDelist, colors = ButtonDefaults.textButtonColors(contentColor = GoogleGrey)) {
                        Icon(Icons.Outlined.VisibilityOff, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("下架")
                    }
                } else if (post.status == "removed" || post.status == "REMOVED" || post.status == "rejected" || post.status == "REJECTED") {
                    TextButton(onClick = onRelist, colors = ButtonDefaults.textButtonColors(contentColor = GoogleBlue)) {
                        Icon(Icons.Outlined.Publish, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("重新发布")
                    }
                }

                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = GoogleGrey)) {
                    Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}
