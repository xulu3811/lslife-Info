package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TradeFeedCard(
    avatarUrl: String?,
    userName: String,
    timeDistanceText: String,
    content: String,
    tags: List<String>,
    imageUrls: List<String>,
    price: Double?,
    onChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xs, horizontal = Dimens.sm),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md)
        ) {
            // Header (Trust Layer)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F6F8))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.sm))
                Column {
                    Text(
                        text = userName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = timeDistanceText,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.sm))

            // Body (Info Layer)
            if (content.isNotBlank()) {
                Text(
                    text = content,
                    fontSize = 15.sp,
                    color = Color(0xFF333333),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(Dimens.sm))
            }

            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0F5F5), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                color = Color(0xFF2B65E3)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.sm))
            }

            // Media (Visual Matrix)
            if (imageUrls.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
                    when (imageUrls.size) {
                        1 -> {
                            AsyncImage(
                                model = imageUrls[0] + "?x-oss-process=image/resize,w_600",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(Color(0xFFF5F6F8))
                            )
                        }
                        2, 3, 4 -> {
                            // 2x2 grid for up to 4 images
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    AsyncImage(
                                        model = imageUrls[0] + "?x-oss-process=image/resize,w_300",
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFFF5F6F8))
                                    )
                                    if (imageUrls.size > 1) {
                                        AsyncImage(
                                            model = imageUrls[1] + "?x-oss-process=image/resize,w_300",
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFFF5F6F8))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                if (imageUrls.size > 2) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        AsyncImage(
                                            model = imageUrls[2] + "?x-oss-process=image/resize,w_300",
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFFF5F6F8))
                                        )
                                        if (imageUrls.size > 3) {
                                            AsyncImage(
                                                model = imageUrls[3] + "?x-oss-process=image/resize,w_300",
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFFF5F6F8))
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // 3x3 grid for 5-9 images
                            val displayImages = imageUrls.take(9)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0..2) {
                                    if (row * 3 < displayImages.size) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            for (col in 0..2) {
                                                val index = row * 3 + col
                                                if (index < displayImages.size) {
                                                    AsyncImage(
                                                        model = displayImages[index] + "?x-oss-process=image/resize,w_300",
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFFF5F6F8))
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.md))
            }

            // Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (price != null && price > 0.0) {
                    Text(
                        text = "¥ $price",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE53935)
                    )
                } else {
                    Text(
                        text = "面议",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "想要",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).clickable { /* TODO: Like */ }
                    )
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "评论",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).clickable { /* TODO: Comment */ }
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE53935), CircleShape)
                            .clickable { onChatClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "一键私聊",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
