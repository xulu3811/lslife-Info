package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingyuan.lslife.core.model.AdminPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentAuditScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onOpenPostDetail: (String) -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPosts("pending_review")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商品/服务上架与修改", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(posts) { post ->
                PostAuditCard(
                    post = post,
                    onApprove = { 
                        viewModel.auditPost(post.id, "approve") { success, msg -> 
                            if (!success) {
                                android.widget.Toast.makeText(context, msg ?: "操作失败", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "审核通过", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } 
                    },
                    onReject = { 
                        viewModel.auditPost(post.id, "reject") { success, msg -> 
                            if (!success) {
                                android.widget.Toast.makeText(context, msg ?: "操作失败", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "已违规下架", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } 
                    },
                    onClick = { onOpenPostDetail(post.id) }
                )
            }
        }
    }
}

@Composable
fun PostAuditCard(
    post: AdminPost,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.description ?: "", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("发布人: ${post.user?.nickname ?: post.user?.phone ?: "未知"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("发布时间: ${post.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("违规下架")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove) {
                    Text("审核通过")
                }
            }
        }
    }
}
