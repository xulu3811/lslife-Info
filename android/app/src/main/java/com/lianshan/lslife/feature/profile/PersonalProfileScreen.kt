package com.qingyuan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.feature.category.ServiceListFeedCard

/** Mock 数据状态 */
data class PersonalProfileState(
    val nickname: String = "闲鱼买家_X89A",
    val avatar: String = "https://picsum.photos/100",
    val authLabel: String = "实名认证用户",
    val joinDays: Int = 120,
    val totalPosts: Int = 15,
    val posts: List<Post> = emptyList() // Mock or real list
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
    state: PersonalProfileState = remember { PersonalProfileState() }
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = scheme.onBackground
                )
            )
        },
        containerColor = Color(0xFFF9F9F9) // Xianyu style off-white background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = state.avatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(scheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.nickname,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Auth Label
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE8F5E9), // Light green background
                        contentColor = Color(0xFF4CAF50)
                    ) {
                        Text(
                            text = state.authLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("加入平台 ${state.joinDays} 天", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("发布过 ${state.totalPosts} 件闲置", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Section Title
                Text(
                    text = "TA的发布",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Posts Feed
            items(state.posts) { post ->
                ServiceListFeedCard(
                    post = post,
                    onClick = { onPostClick(post.id) },
                    onCall = { /* Handle call or mock it */ },
                    onChat = { /* Handle chat or mock it */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
