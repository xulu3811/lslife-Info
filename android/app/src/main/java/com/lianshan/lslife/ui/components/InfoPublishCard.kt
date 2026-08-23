package com.lianshan.lslife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post
import com.lianshan.lslife.core.model.TradeMode

@Composable
fun InfoPublishCard(
    post: Post,
    onClick: () -> Unit = {},
    onPhoneClick: () -> Unit = onClick,
    onChatClick: () -> Unit = onClick
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0F000000),
                ambientColor = Color(0x0A000000)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            val cover = post.images.firstOrNull()
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(Color(0xFFF7F8FA)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                Text(
                    post.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Avatar only (no nickname)
                    if (post.user?.avatar != null) {
                        AsyncImage(
                            model = post.user.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF4F5F7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF999999), modifier = Modifier.size(16.dp))
                        }
                    }

                    // Right: Call and Chat Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Enhanced Call Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F9F0))
                                .clickable { onPhoneClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Phone, contentDescription = "电话", tint = Color(0xFF52C41A), modifier = Modifier.size(16.dp))
                        }
                        // Enhanced Chat Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF1F0))
                                .clickable { onChatClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "私聊", tint = Color(0xFFFF4D4F), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
