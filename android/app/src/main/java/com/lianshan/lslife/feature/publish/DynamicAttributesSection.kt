package com.qingyuan.lslife.feature.publish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingyuan.lslife.core.model.AttributeSchema
import com.qingyuan.lslife.core.model.FieldType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DynamicAttributesSection(
    schemas: List<AttributeSchema>,
    attributesMap: Map<String, Any>,
    onAttributeChange: (key: String, value: Any) -> Unit,
    modifier: Modifier = Modifier
) {
    if (schemas.isEmpty()) return

    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DisplaySettings,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "专属规则与分类属性",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF222222)
                    )
                }
                Text(
                    text = "根据分类动态配置",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Dynamic Fields
            schemas.forEach { schema ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schema.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        if (schema.isRequired) {
                            Spacer(Modifier.width(4.dp))
                            Text("*", color = scheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    when (schema.type) {
                        FieldType.SINGLE_CHOICE -> {
                            val selectedVal = attributesMap[schema.key]?.toString() ?: ""
                            AttributeChipGroup(
                                options = schema.options,
                                selectedOptions = if (selectedVal.isEmpty()) emptySet() else setOf(selectedVal),
                                isMultiSelect = false,
                                onOptionSelected = { option ->
                                    val newValue = if (selectedVal == option) "" else option
                                    onAttributeChange(schema.key, newValue)
                                }
                            )
                        }

                        FieldType.MULTI_CHOICE -> {
                            val rawVal = attributesMap[schema.key]
                            val selectedSet = when (rawVal) {
                                is Set<*> -> rawVal.mapNotNull { it?.toString() }.toSet()
                                is List<*> -> rawVal.mapNotNull { it?.toString() }.toSet()
                                is String -> if (rawVal.isBlank()) emptySet() else rawVal.split(",").toSet()
                                else -> emptySet()
                            }
                            AttributeChipGroup(
                                options = schema.options,
                                selectedOptions = selectedSet,
                                isMultiSelect = true,
                                onOptionSelected = { option ->
                                    val newSet = if (selectedSet.contains(option)) {
                                        selectedSet - option
                                    } else {
                                        selectedSet + option
                                    }
                                    onAttributeChange(schema.key, newSet.toList())
                                }
                            )
                        }

                        FieldType.NUMBER_INPUT -> {
                            val currentNum = attributesMap[schema.key]?.toString() ?: ""
                            AttributeNumberField(
                                value = currentNum,
                                unit = schema.unit,
                                onValueChange = { newVal ->
                                    onAttributeChange(schema.key, newVal)
                                }
                            )
                        }

                        FieldType.TEXT_INPUT -> {
                            val currentText = attributesMap[schema.key]?.toString() ?: ""
                            AttributeTextField(
                                value = currentText,
                                onValueChange = { newVal ->
                                    onAttributeChange(schema.key, newVal)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttributeChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    isMultiSelect: Boolean,
    onOptionSelected: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            val bgColor = if (isSelected) scheme.primaryContainer else Color(0xFFF7F8FA)
            val textColor = if (isSelected) scheme.primary else Color(0xFF555555)
            val borderColor = if (isSelected) scheme.primary.copy(alpha = 0.5f) else Color.Transparent

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onOptionSelected(option) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun AttributeNumberField(
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF7F8FA))
            .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF333333), fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("请输入数值", fontSize = 14.sp, color = Color(0xFFBBBBBB))
                }
                innerTextField()
            }
        )
        if (unit.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(text = unit, fontSize = 14.sp, color = scheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AttributeTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF7F8FA))
            .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF333333)),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("请输入相关描述", fontSize = 14.sp, color = Color(0xFFBBBBBB))
                }
                innerTextField()
            }
        )
    }
}
