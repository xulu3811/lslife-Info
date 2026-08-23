package com.lianshan.lslife.feature.publish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.lianshan.lslife.core.model.AttributeSchema
import com.lianshan.lslife.core.model.FieldType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DynamicAttributesSection(
    schemas: List<AttributeSchema>,
    attributesMap: Map<String, Any>,
    onAttributeChange: (key: String, value: Any) -> Unit,
    modifier: Modifier = Modifier
) {
    if (schemas.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE52F2F))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "专属规则与分类属性",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }
                Text(
                    text = "根据已选分类动态配置",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            HorizontalDivider(color = Color(0xFFF5F5F5))

            schemas.forEach { schema ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schema.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF444444)
                        )
                        if (schema.isRequired) {
                            Text(" *", color = Color(0xFFE52F2F), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

/** 芯片选择组 (单选 / 多选) */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttributeChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    isMultiSelect: Boolean,
    onOptionSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            val bgColor = if (isSelected) Color(0xFFFFF0F0) else Color(0xFFF5F6F8)
            val textColor = if (isSelected) Color(0xFFE52F2F) else Color(0xFF555555)
            val borderColor = if (isSelected) Color(0xFFE52F2F) else Color.Transparent

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onOptionSelected(option) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

/** 数字带单位输入框 */
@Composable
private fun AttributeNumberField(
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F6F8))
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    Text("请输入数值", fontSize = 14.sp, color = Color.Gray)
                }
                innerTextField()
            }
        )
        if (unit.isNotEmpty()) {
            Spacer(Modifier.width(6.dp))
            Text(text = unit, fontSize = 14.sp, color = Color(0xFF666666), fontWeight = FontWeight.Medium)
        }
    }
}

/** 文本输入框 */
@Composable
private fun AttributeTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F6F8))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF333333)),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("请输入相关描述", fontSize = 14.sp, color = Color.Gray)
                }
                innerTextField()
            }
        )
    }
}
