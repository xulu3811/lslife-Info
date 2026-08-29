package com.qingyuan.lslife.feature.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qingyuan.lslife.R
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.core.model.PostType
import com.qingyuan.lslife.ui.theme.Dimens
import kotlin.random.Random

// --- Theme Engine ---
sealed class PostTheme(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val contentColor: Color,
    val subContentColor: Color,
    val tagColor: Color,
    val iconTint: Color,
    val borderStroke: BorderStroke? = null,
    val shadowColor: Color = Color.LightGray
) {
    object ClayPark : PostTheme(
        backgroundColor = Color(0xFFF3F1ED),
        surfaceColor = Color.White,
        contentColor = Color(0xFF4A443E),
        subContentColor = Color(0xFF9E9995),
        tagColor = Color(0xFFE5DECB),
        iconTint = Color(0xFFE57A60),
        borderStroke = BorderStroke(2.dp, Color(0xFFE5DECB))
    )
    object Cyberpunk : PostTheme(
        backgroundColor = Color(0xFF0F0B29),
        surfaceColor = Color(0xFF1B163B),
        contentColor = Color(0xFF00FFCC),
        subContentColor = Color(0xFF8B85C1),
        tagColor = Color(0xFFE01E85),
        iconTint = Color(0xFFFF0055),
        borderStroke = BorderStroke(1.dp, Color(0xFF00FFCC)),
        shadowColor = Color(0xFFFF0055)
    )
    object Macaron : PostTheme(
        backgroundColor = Color(0xFFFDF6F8),
        surfaceColor = Color(0xFFFFF0F5),
        contentColor = Color(0xFF6B5B95),
        subContentColor = Color(0xFFA8A3C4),
        tagColor = Color(0xFFFFB6C1),
        iconTint = Color(0xFFFF69B4),
        shadowColor = Color(0x33FF69B4)
    )
    object RetroPixel : PostTheme(
        backgroundColor = Color(0xFFD4E157),
        surfaceColor = Color(0xFF8BC34A),
        contentColor = Color.Black,
        subContentColor = Color(0xFF333333),
        tagColor = Color.White,
        iconTint = Color.Black,
        borderStroke = BorderStroke(3.dp, Color.Black)
    )
    object Newspaper : PostTheme(
        backgroundColor = Color(0xFFEBEBEB),
        surfaceColor = Color(0xFFF7F7F7),
        contentColor = Color(0xFF111111),
        subContentColor = Color(0xFF555555),
        tagColor = Color(0xFFDEDEDE),
        iconTint = Color(0xFF222222),
        borderStroke = BorderStroke(1.dp, Color(0xFF333333))
    )
    
    // Fallback for generating defaults
    companion object {
        fun random(): PostTheme {
            return listOf(ClayPark, Cyberpunk, Macaron, RetroPixel, Newspaper).random()
        }
    }
}

@Composable
fun CityStrollFeedScreen(
    posts: List<Post>,
    onPostClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)) // Pure light gray background
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(posts, key = { it.id }) { post ->
                GallerySkinFeedCard(post = post, onClick = { onPostClick(post.id) })
            }
        }
    }
}

@Composable
fun GallerySkinFeedCard(post: Post, onClick: () -> Unit) {
    // 根据 post.id 生成稳定的伪随机高度，实现真正的小红书双列瀑布流视觉
    val imageHeight = remember(post.id) {
        val seed = post.id.hashCode().toLong()
        val random = kotlin.random.Random(seed)
        random.nextInt(140, 260).dp
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, Color(0xFFEEEEEE)),
        shadowElevation = 0.dp
    ) {
        Column {
            // Layer 2 (商品/种草主图层)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = post.images.firstOrNull()?.plus("?x-oss-process=image/resize,w_400") ?: "",
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                )
                
                // Sponsored Badge or COMMERCE Tag
                if (post.postType == com.qingyuan.lslife.core.model.PostType.CLASSIFIED && post.price != null) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(999.dp) // Capsule shape
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "¥ ${post.price}",
                            color = Color(0xFFFF4D4F), // Red text
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (post.isSponsored) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔥 热门",
                            color = Color(0xFFFFD700),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Layer 3 (信息承载层) - Typography
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                Text(
                    text = post.title,
                    color = Color(0xFF222222), // High contrast text
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                if (post.linkedCommerceId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // 升级版种草商品锚点 UI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF4F4), RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color(0xFFFFE0E0), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable { /* It will trigger the card onClick but you can also handle specifically here if needed */ },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.ShoppingBag,
                                contentDescription = "Commodity",
                                tint = Color(0xFFFF5000), // Taobao/Joybuy Orange
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "去抢购同款服务",
                                color = Color(0xFFFF5000),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "GO ❯",
                            color = Color(0xFFFF5000),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // User Info
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = post.user?.avatar,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(18.dp) // Micro avatar
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.user?.nickname ?: "匿名用户",
                            color = Color(0xFF999999),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Floating Like/Comment Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like",
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = post.likeCount.toString(),
                            color = Color(0xFF999999),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCityStrollFeedScreen() {
    CityStrollFeedScreen(posts = emptyList(), onPostClick = {})
}
