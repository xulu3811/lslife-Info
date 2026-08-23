package com.lianshan.lslife.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.core.model.CategorySchemaResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterBottomSheet(
    schema: CategorySchemaResponse?,
    categoryTree: List<com.lianshan.lslife.core.model.CategoryNode>,
    selectedCategory: String?,
    publisherType: String?,
    listingType: String?,
    minPrice: Double?,
    maxPrice: Double?,
    attributesFilter: Map<String, Set<String>>,
    aggregations: Map<String, Map<String, Int>> = emptyMap(),
    onPublisherTypeChange: (String?) -> Unit,
    onListingTypeChange: (String?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onPriceChange: (Double?, Double?) -> Unit,
    onAttributeToggle: (String, String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var minPriceText by remember(minPrice) { mutableStateOf(minPrice?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var maxPriceText by remember(maxPrice) { mutableStateOf(maxPrice?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "高级筛选·属性规配",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = {
                    minPriceText = ""
                    maxPriceText = ""
                    onCategoryChange(null)
                    onReset()
                }) {
                    Text("全部重置", color = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 业务分类筛选
            if (categoryTree.isNotEmpty()) {
                Text(
                    text = "商品/服务分类",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null || selectedCategory == "all",
                            onClick = { onCategoryChange(null) },
                            label = { Text("全部") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    items(categoryTree) { node ->
                        var isSelected = false
                        fun checkSelected(n: com.lianshan.lslife.core.model.CategoryNode, target: String?): Boolean {
                            if (target == null) return false
                            if (n.id == target) return true
                            return n.children.any { checkSelected(it, target) }
                        }
                        isSelected = checkSelected(node, selectedCategory)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(node.id) },
                            label = { Text(node.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 全局基础筛选 (发布者身份 / 服务类型)
            Text(
                text = "来源类型",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                val publishers = listOf(null to "全部", "INDIVIDUAL" to "个人发布", "MERCHANT" to "认证商家")
                items(publishers) { (v, label) ->
                    val isSelected = publisherType == v
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPublisherTypeChange(v) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Text(
                text = "交易类型",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                val listings = listOf(null to "全部", "GOODS" to "实体商品", "SERVICE" to "本地服务")
                items(listings) { (v, label) ->
                    val isSelected = listingType == v
                    FilterChip(
                        selected = isSelected,
                        onClick = { onListingTypeChange(v) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 价格区间 (Price Range)
            Text(
                text = "价格区间 (元)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = { 
                        minPriceText = it.filter { c -> c.isDigit() || c == '.' }
                        onPriceChange(minPriceText.toDoubleOrNull(), maxPriceText.toDoubleOrNull())
                    },
                    placeholder = { Text("最低价", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Text("—", color = MaterialTheme.colorScheme.outline)
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { 
                        maxPriceText = it.filter { c -> c.isDigit() || c == '.' }
                        onPriceChange(minPriceText.toDoubleOrNull(), maxPriceText.toDoubleOrNull())
                    },
                    placeholder = { Text("最高价", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 价格快捷药丸
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 16.dp)
            ) {
                val priceRanges = listOf(
                    "0-100" to (0.0 to 100.0),
                    "100-500" to (100.0 to 500.0),
                    "500-2000" to (500.0 to 2000.0),
                    "2000以上" to (2000.0 to null)
                )
                items(priceRanges) { (label, range) ->
                    val isSelected = (minPrice == range.first) && (maxPrice == range.second)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                minPriceText = ""
                                maxPriceText = ""
                                onPriceChange(null, null)
                            } else {
                                minPriceText = range.first?.toInt()?.toString() ?: ""
                                maxPriceText = range.second?.toInt()?.toString() ?: ""
                                onPriceChange(range.first, range.second)
                            }
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // 动态规格属性筛选 (Dynamic Attributes)
            schema?.attributeSchema?.filter { it.fieldType == "SELECT" && it.options.isNotEmpty() }?.forEach { field ->
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
                OptFlowRow(
                    options = field.options,
                    selectedVals = attributesFilter[field.label] ?: attributesFilter[field.key],
                    counts = aggregations[field.key] ?: aggregations[field.label],
                    onSelect = { onAttributeToggle(field.label, it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer action
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "查看筛选结果",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptFlowRow(
    options: List<String>,
    selectedVals: Set<String>?,
    counts: Map<String, Int>? = null,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { opt ->
            val isSelected = selectedVals?.contains(opt) == true
            val count = counts?.get(opt)
            val labelText = if (count != null) "$opt ($count)" else opt
            
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(opt) },
                label = { Text(labelText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
