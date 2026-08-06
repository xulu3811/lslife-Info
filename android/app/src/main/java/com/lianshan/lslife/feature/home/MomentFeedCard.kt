package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.lianshan.lslife.core.model.LinkedCommerce

@Composable
fun MomentFeedCard(
    avatarUrl: String?,
    userName: String,
    title: String,
    description: String,
    imageUrl: String?,
    likeCount: Int,
    linkedCommerce: LinkedCommerce?,
    onCardClick: () -> Unit,
    onLinkedCommerceClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl + "?x-oss-process=image/resize,w_400",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 280.dp)
                        .background(Color(0xFFF5F6F8))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(Color(0xFFF5F6F8))
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                val displayText = title.ifBlank { description }
                if (displayText.isNotBlank()) {
                    Text(
                        text = displayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F6F8))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0))
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userName,
                            fontSize = 11.sp,
                            color = Color(0xFF999999),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FavoriteBorder,
                            contentDescription = "喜欢",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (likeCount > 0) likeCount.toString() else "赞",
                            fontSize = 11.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                if (linkedCommerce != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF0F0), RoundedCornerShape(4.dp))
                            .clickable { onLinkedCommerceClick(linkedCommerce.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = "购买同款",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "看同款",
                                fontSize = 11.sp,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (linkedCommerce.price != null && linkedCommerce.price > 0) {
                                Text(
                                    text = "¥${linkedCommerce.price}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
