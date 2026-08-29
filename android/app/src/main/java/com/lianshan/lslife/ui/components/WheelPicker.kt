package com.qingyuan.lslife.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    modifier: Modifier = Modifier,
    items: List<T>,
    itemHeight: Dp = 48.dp,
    visibleItemsCount: Int = 5,
    startIndex: Int = 0,
    onItemSelected: (index: Int, item: T) -> Unit,
    itemToString: (T) -> String = { it.toString() }
) {
    // Determine a safe valid index
    val safeStartIndex = if (items.isNotEmpty()) startIndex.coerceIn(0, items.lastIndex) else 0
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeStartIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val halfVisible = visibleItemsCount / 2
    
    val paddedItems = remember(items) {
        val pad = List(halfVisible) { null }
        pad + items + pad
    }

    // Trigger onItemSelected when the first visible item changes
    LaunchedEffect(listState, items) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { it }
            .distinctUntilChanged()
            .collect { index ->
                if (items.isNotEmpty() && index in items.indices) {
                    onItemSelected(index, items[index])
                }
            }
    }

    // React to items list changes and reset scroll
    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            listState.scrollToItem(0)
            onItemSelected(0, items[0])
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        // Center selection highlight line
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
        ) {
            drawLine(
                color = Color.LightGray,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.LightGray,
                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize()
        ) {
            items(paddedItems.size) { index ->
                val item = paddedItems[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    if (item != null) {
                        val isCenter = index == listState.firstVisibleItemIndex + halfVisible
                        val distance = abs(index - (listState.firstVisibleItemIndex + halfVisible))
                        
                        val alpha = when (distance) {
                            0 -> 1f
                            1 -> 0.6f
                            2 -> 0.3f
                            else -> 0.1f
                        }
                        
                        val fontSize = if (isCenter) 16.sp else 14.sp
                        val fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal
                        val color = if (isCenter) Color(0xFFE53935) else Color.Black

                        Text(
                            text = itemToString(item),
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            color = color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(alpha)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
