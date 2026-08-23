package com.lianshan.lslife.feature.publish

/**
 * 从标题/描述中提取品牌、型号参数、购买日期。
 * 仅作引导预填，用户可随时改写。
 */
object PublishMetaExtractor {

    /** 按长度降序匹配，避免「小米」误伤「小米有品」等短词优先问题 */
    private val knownBrands = listOf(
        "Insta360", "GoPro", "Dyson", "戴森", "Nintendo", "任天堂",
        "Apple", "苹果", "iPhone", "iPad", "MacBook",
        "华为", "Huawei", "荣耀", "Honor", "小米", "Xiaomi", "红米", "Redmi",
        "OPPO", "vivo", "一加", "OnePlus", "三星", "Samsung",
        "索尼", "Sony", "佳能", "Canon", "尼康", "Nikon",
        "联想", "Lenovo", "戴尔", "Dell", "惠普", "HP", "华硕", "ASUS",
        "大疆", "DJI", "美的", "海尔", "格力", "飞利浦", "Philips",
        "耐克", "Nike", "阿迪达斯", "Adidas", "优衣库", "无印良品",
    ).sortedByDescending { it.length }

    private val datePatterns = listOf(
        Regex("""(20\d{2})[.\-/年](\d{1,2})[.\-/月](\d{1,2})日?"""),
        Regex("""(20\d{2})年(\d{1,2})月(\d{1,2})?日?"""),
        Regex("""(20\d{2})[.\-/](\d{1,2})[.\-/](\d{1,2})"""),
    )

    data class Extracted(
        val brand: String? = null,
        val params: String? = null,
        val purchaseDate: String? = null,
        val itemKind: String? = null,
    )

    fun extract(title: String, description: String): Extracted {
        val text = "$title\n$description".trim()
        if (text.isBlank()) return Extracted()

        val brand = extractBrand(text)
        val purchaseDate = extractPurchaseDate(text)
        val params = extractParams(title, description, brand)
        val itemKind = extractKind(text)

        return Extracted(
            brand = brand,
            params = params,
            purchaseDate = purchaseDate,
            itemKind = itemKind,
        )
    }

    private fun extractBrand(text: String): String? {
        for (b in knownBrands) {
            if (text.contains(b, ignoreCase = true)) {
                return normalizeBrand(b)
            }
        }
        // 标题首词为英文品牌形态：Insta360 / DJI 等
        val first = titleFirstToken(text)
        if (first != null && first.length in 2..20 && first.any { it.isLetter() }) {
            return first
        }
        return null
    }

    private fun titleFirstToken(text: String): String? {
        val firstLine = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: return null
        val token = firstLine.split(Regex("""[\s，,、/|]+""")).firstOrNull().orEmpty()
        if (token.matches(Regex("""^[A-Za-z][A-Za-z0-9.\-]{1,19}$"""))) return token
        return null
    }

    private fun normalizeBrand(raw: String): String = when (raw.lowercase()) {
        "apple", "iphone", "ipad", "macbook" -> "Apple"
        "huawei" -> "华为"
        "honor" -> "荣耀"
        "xiaomi", "redmi" -> "小米"
        "samsung" -> "三星"
        "sony" -> "索尼"
        "dji" -> "大疆"
        "nike" -> "耐克"
        "adidas" -> "阿迪达斯"
        else -> raw
    }

    private fun extractPurchaseDate(text: String): String? {
        for (p in datePatterns) {
            val m = p.find(text) ?: continue
            val y = m.groupValues.getOrNull(1) ?: continue
            val mo = m.groupValues.getOrNull(2)?.padStart(2, '0') ?: continue
            val d = m.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.padStart(2, '0')
            return if (d != null) "$y-$mo-$d" else "$y-$mo"
        }
        return null
    }

    private fun extractParams(title: String, description: String, brand: String?): String? {
        val t = title.trim()
        if (t.isNotBlank()) {
            var rest = t
            if (brand != null) {
                rest = rest.replace(brand, "", ignoreCase = true).trim()
                // 去掉品牌别名
                knownBrands.filter { normalizeBrand(it).equals(brand, true) || it.equals(brand, true) }
                    .forEach { rest = rest.replace(it, "", ignoreCase = true) }
            }
            rest = rest.trim(' ', '-', '·', '—', '/', '，', ',')
            if (rest.length in 2..60) return rest.take(80)
        }
        // 描述里常见型号片段
        val model = Regex(
            """(?i)(flow\s*2\s*pro|iphone\s*\d+\s*(pro\s*max|pro|plus)?|[a-z]*\d+[a-z]*(?:\s*pro)?)""",
        ).find(description)?.value?.trim()
        return model?.take(80)
    }

    private fun extractKind(text: String): String? {
        val rules = listOf(
            listOf("云台", "相机", "镜头", "gopro", "insta360") to "手机数码",
            listOf("手机", "iphone", "华为手机") to "手机数码",
            listOf("电脑", "笔记本", "平板", "ipad") to "电脑办公",
            listOf("空调", "冰箱", "洗衣机", "电器") to "家用电器",
            listOf("衣服", "鞋", "包", "服饰") to "服饰鞋包",
            listOf("沙发", "床", "桌", "家具") to "家具家居",
            listOf("婴儿", "奶粉", "童车") to "母婴用品",
            listOf("保洁", "家政", "月嫂") to "本地服务",
            listOf("搬家", "货运") to "本地服务",
        )
        val lower = text.lowercase()
        for ((keys, kind) in rules) {
            if (keys.any { lower.contains(it.lowercase()) }) return kind
        }
        return null
    }
}
