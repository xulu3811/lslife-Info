package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post

@Composable
fun ClayCard(modifier: Modifier = Modifier, onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun AsymmetricFeaturedLayout(
    posts: List<Post>,
    onPostClick: (String) -> Unit
) {
    if (posts.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(240.dp) // 固定高度以保持整洁
    ) {
        // A. 大的特色卡片
        FeaturedProductCard(
            post = posts[0],
            modifier = Modifier
                .weight(0.67f) // 占 2/3 宽度
                .fillMaxHeight()
                .padding(end = 8.dp),
            onClick = { onPostClick(posts[0].id) }
        )

        // B. 两个垂直堆叠的小卡片
        Column(
            modifier = Modifier
                .weight(0.33f) // 占 1/3 宽度
                .fillMaxHeight()
        ) {
            // 上面的小卡片
            if (posts.size > 1) {
                CompactAccessoryCard(
                    post = posts[1],
                    modifier = Modifier
                        .weight(1f) // 均分高度
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    onClick = { onPostClick(posts[1].id) }
                )
            }
            // 下面的小卡片
            if (posts.size > 2) {
                CompactAccessoryCard(
                    post = posts[2],
                    modifier = Modifier
                        .weight(1f) // 均分高度
                        .fillMaxWidth(),
                    onClick = { onPostClick(posts[2].id) }
                )
            }
        }
    }
}

@Composable
fun FeaturedProductCard(post: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ClayCard(modifier = modifier, onClick = onClick) {
        Column {
            val imageUrl = post.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl + "?x-oss-process=image/resize,w_600",
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF5F6F8)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF5F6F8))
                )
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                val displayText = post.title.ifBlank { post.description }
                Text(
                    text = displayText, 
                    maxLines = 1, 
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val price = post.price
                if (price != null && price > 0) {
                    Text(
                        text = "¥$price",
                        fontSize = 14.sp,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CompactAccessoryCard(post: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ClayCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imageUrl = post.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl + "?x-oss-process=image/resize,w_200",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF5F6F8)),
                    contentScale = ContentScale.Crop,
                    contentDescription = post.title
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF5F6F8))
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                val displayText = post.title.ifBlank { post.description }
                Text(
                    text = displayText, 
                    maxLines = 1, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color(0xFF333333),
                    overflow = TextOverflow.Ellipsis
                )
                val price = post.price
                if (price != null && price > 0) {
                    Text(
                        text = "¥$price", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CompactProductCard(post: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ClayCard(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = post.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl + "?x-oss-process=image/resize,w_300",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F6F8)),
                    contentScale = ContentScale.Crop,
                    contentDescription = post.title
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F6F8))
                )
            }
            
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                val displayText = post.title.ifBlank { post.description }
                Text(
                    text = displayText, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                val price = post.price
                if (price != null && price > 0) {
                    Text(
                        text = "¥$price", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
