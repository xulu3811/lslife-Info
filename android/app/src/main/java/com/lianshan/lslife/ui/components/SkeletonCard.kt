package com.lianshan.lslife.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.ui.theme.Dimens

@Composable
fun ShimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translation"
        )
        val colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        )
        Brush.linearGradient(
            colors = colors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val brush = ShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimens.md)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
            // Image skeleton
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(Dimens.sm))
                    .background(brush)
            )
            // Text lines skeleton
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}
