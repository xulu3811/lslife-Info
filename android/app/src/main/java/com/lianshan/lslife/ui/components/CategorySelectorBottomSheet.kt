package com.lianshan.lslife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorBottomSheet(
    parentCategory: CategoryNode,
    onDismissRequest: () -> Unit,
    onCategorySelected: (categoryId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Left column level-2 category selection
    var selectedL2 by remember { mutableStateOf(parentCategory.children.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null // We will provide our own header
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // Occupy 70% of screen height
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.md, vertical = Dimens.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "选择${parentCategory.name}分类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            // Dual pane layout
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Left Pane: Level 2 Categories
                LazyColumn(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    items(parentCategory.children) { l2Node ->
                        val isSelected = selectedL2?.id == l2Node.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent
                                )
                                .clickable { selectedL2 = l2Node },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                // Indicator strip
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary)
                                        .align(Alignment.CenterStart)
                                )
                            }
                            Text(
                                text = l2Node.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Right Pane: Level 3 Categories
                val l3Children = selectedL2?.children ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(Dimens.md),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    selectedL2?.let { l2Node ->
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { onCategorySelected(l2Node.id) }
                                    .padding(vertical = Dimens.sm),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CategoryIconView(iconUrl = null, iconName = "all", size = 56.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "全部" + l2Node.name.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    items(l3Children) { l3Node ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onCategorySelected(l3Node.id) }
                                .padding(vertical = Dimens.sm),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = MaterialTheme.shapes.medium,
                                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surface,
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                        ),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIconView(
                                    iconUrl = l3Node.iconUrl, 
                                    iconName = l3Node.icon, 
                                    categoryName = l3Node.name, 
                                    size = 36.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = l3Node.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
