package com.qingyuan.lslife.core.model

/** 属性字段类型枚举 */
enum class FieldType {
    SINGLE_CHOICE,   // 单选 (如：朝向、户型、学历) -> 渲染为 ChipGroup
    MULTI_CHOICE,    // 多选 (如：特色、福利、服务包含) -> 渲染为 FilterChip Group
    NUMBER_INPUT,    // 数字输入 (如：面积、工作年限)
    TEXT_INPUT       // 文本输入
}

/** 动态属性字段定义 Schema */
data class AttributeSchema(
    val key: String,                  // 存入DB的字段名 (如 "area", "orientation")
    val label: String,                // UI展示名 (如 "面积 (㎡)", "朝向")
    val type: FieldType,              // 控件渲染类型
    val options: List<String> = emptyList(), // 可选的选项列表
    val unit: String = "",            // 输入框后缀单位 (如 "㎡", "年", "元")
    val isRequired: Boolean = false   // 是否必填
)
