package com.lianshan.lslife.feature.publish

import com.lianshan.lslife.core.model.TradeMode

data class CategoryConfig(
    val id: String,
    val name: String,
    val tradeMode: TradeMode = TradeMode.INFO_PUBLISH,
    /** 使用引导填写（种类/品牌/参数），不再展示属性芯片 */
    val guidedFill: Boolean = false,
    val attr1Label: String? = null,
    val attr1Options: List<String> = emptyList(),
    val attr2Label: String? = null,
    val attr2Options: List<String> = emptyList(),
)

/** 个人闲置：商品/服务种类快捷建议 */
val secondHandKindSuggestions = listOf(
    "手机数码", "电脑办公", "家用电器", "服饰鞋包",
    "家具家居", "母婴用品", "美妆护肤", "运动户外",
    "图书文娱", "宠物用品", "本地服务", "其他闲置",
)

/** 个人闲置：品牌快捷建议（点选后写入输入框，可再改） */
val secondHandBrandSuggestions = listOf(
    "Insta360", "Apple", "华为", "小米", "OPPO", "vivo", "三星", "大疆", "联想", "其他",
)

/** 个人闲置：成色（紧凑单选，写入 condition） */
val secondHandConditionOptions = listOf(
    "全新", "几乎全新", "轻微使用痕迹", "明显使用痕迹",
)

val publishCategoryConfigs = listOf(
    CategoryConfig(
        id = "second_hand",
        name = "个人闲置",
        guidedFill = true,
    ),
    CategoryConfig(
        id = "cat_job",
        name = "求职招聘",
        attr1Label = "类型",
        attr1Options = listOf("全职", "兼职", "实习", "日结"),
        attr2Label = "经验",
        attr2Options = listOf("不限", "1年以内", "1-3年", "3-5年", "5年以上"),
    ),
    CategoryConfig(
        id = "cat_house_sale",
        name = "二手房源",
        attr1Label = "类型",
        attr1Options = listOf("住宅", "别墅", "商铺", "厂房"),
        attr2Label = "户型",
        attr2Options = listOf("1室", "2室", "3室", "4室及以上"),
    ),
    CategoryConfig(
        id = "cat_house_rent",
        name = "租房",
        attr1Label = "方式",
        attr1Options = listOf("整套出租", "单间合租", "床位出租", "商铺办公"),
        attr2Label = "户型",
        attr2Options = listOf("1室", "2室", "3室", "4室及以上"),
    ),
    CategoryConfig(
        id = "housekeeping",
        name = "家政保洁",
        attr1Label = "服务",
        attr1Options = listOf("日常保洁", "深度保洁", "开荒保洁", "月嫂/保姆"),
    ),
    CategoryConfig(
        id = "maintenance",
        name = "水电维修",
        attr1Label = "类别",
        attr1Options = listOf("家电维修", "水管维修", "电路维修", "房屋修缮"),
    ),
    CategoryConfig(
        id = "moving",
        name = "货运搬家",
        attr1Label = "车型",
        attr1Options = listOf("小面包车", "中面包车", "小货车", "中货车"),
        attr2Label = "搬运",
        attr2Options = listOf("需搬运", "仅拉货"),
    ),
    CategoryConfig(
        id = "veggies",
        name = "同城生鲜",
        attr1Label = "分类",
        attr1Options = listOf("新鲜水果", "蔬菜", "农副产品"),
    ),
    CategoryConfig(
        id = "part_time",
        name = "同城兼职",
        attr1Label = "岗位",
        attr1Options = listOf("小时工/钟点工", "周末兼职", "晚间兼职", "寒暑假工", "发传单/促销"),
        attr2Label = "结算",
        attr2Options = listOf("日结", "周结", "完工结", "月结"),
    ),
    CategoryConfig(
        id = "secondhand_house",
        name = "二手房源",
        attr1Label = "类型",
        attr1Options = listOf("住宅小区", "自建房/独立栋", "临街商住两用", "公寓/复式"),
        attr2Label = "户型",
        attr2Options = listOf("2室1厅", "3室2厅", "4室2厅及以上", "独栋楼房"),
    ),
    CategoryConfig(
        id = "shop_rent",
        name = "旺铺转让",
        attr1Label = "方式",
        attr1Options = listOf("整租转让", "分租合租", "写字楼/办公", "仓库厂房"),
        attr2Label = "地段",
        attr2Options = listOf("县城中心商业街", "广场商圈", "小区临街商铺", "吉潭镇/其他乡镇"),
    ),
)

fun isPersonalIdleCategory(categoryId: String?): Boolean {
    if (categoryId == null || categoryId == "all") return true
    if (categoryId == "second_hand" || categoryId == "cat_idle") return true
    val idlePrefixes = listOf("cat_3c", "cat_clothing", "cat_dress", "cat_bag", "cat_luxury", "cat_home", "cat_beauty", "cat_baby", "cat_sports", "cat_hobby", "cat_other")
    return idlePrefixes.any { categoryId.startsWith(it) }
}

fun getCategoryConfig(categoryId: String): CategoryConfig {
    if (isPersonalIdleCategory(categoryId)) {
        return publishCategoryConfigs.find { it.id == "second_hand" } ?: publishCategoryConfigs.first()
    }
    return publishCategoryConfigs.find { it.id == categoryId } ?: publishCategoryConfigs.first()
}

/** 根据引导字段拼一段可编辑的描述草稿 */
fun buildGuidedDescription(
    itemKind: String,
    brand: String,
    params: String,
    condition: String,
    purchaseDate: String = "",
): String {
    val lines = mutableListOf<String>()
    val head = listOfNotNull(
        itemKind.takeIf { it.isNotBlank() },
        brand.takeIf { it.isNotBlank() },
        params.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
    if (head.isNotBlank()) lines += head
    if (condition.isNotBlank()) lines += "成色：$condition"
    if (purchaseDate.isNotBlank()) lines += "购买日期：$purchaseDate"
    if (lines.isEmpty()) return ""
    lines += "同城自提优先，有意私聊。"
    return lines.joinToString("\n")
}

fun getEffectiveTradeMode(postTradeMode: TradeMode, categoryId: String?): TradeMode {
    if (categoryId == null) return postTradeMode
    val infoPrefixes = listOf("cat_house", "cat_job", "cat_part_time", "cat_car_rental", "cat_education", "job_", "house_", "car_", "edu_", "part_time_")
    if (infoPrefixes.any { categoryId.startsWith(it) }) return TradeMode.INFO_PUBLISH
    
    val commercePrefixes = listOf("cat_idle", "cat_veggies", "cat_service", "cat_maintenance", "cat_dining", "second_hand", "cat_3c", "cat_clothing", "cat_dress", "cat_bag", "cat_luxury", "cat_home", "cat_beauty", "cat_baby", "cat_sports", "cat_hobby", "cat_other", "fresh_", "food_", "housekeeping_", "repair_")
    if (commercePrefixes.any { categoryId.startsWith(it) }) return TradeMode.COMMERCE
    
    return postTradeMode
}
