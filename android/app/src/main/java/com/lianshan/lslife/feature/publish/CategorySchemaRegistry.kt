package com.qingyuan.lslife.feature.publish

import com.qingyuan.lslife.core.model.AttributeSchema
import com.qingyuan.lslife.core.model.FieldType

object CategorySchemaRegistry {

    /** 根据分类 ID 或 分类名称匹配动态属性 Schema 列表 */
    fun getCategorySchema(categoryId: String?, categoryName: String? = null): List<AttributeSchema> {
        val id = categoryId?.lowercase() ?: ""
        val name = categoryName ?: ""

        return when {
            // 5. 房屋出租 & 6. 二手房产
            id.startsWith("cat_5_rent") || id.startsWith("cat_6_sale") -> listOf(
                AttributeSchema(key = "layout", label = "户型", type = FieldType.SINGLE_CHOICE, options = listOf("1室", "2室", "3室", "4室及以上", "单间"), isRequired = true),
                AttributeSchema(key = "area", label = "建筑面积", type = FieldType.NUMBER_INPUT, unit = "㎡", isRequired = true),
                AttributeSchema(key = "orientation", label = "房屋朝向", type = FieldType.SINGLE_CHOICE, options = listOf("南", "南北", "东", "西", "北")),
                AttributeSchema(key = "features", label = "房源特色 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("满五唯一", "近地铁", "精装修", "电梯房", "随时看房", "采光极佳", "带车位"))
            )

            // 8. 招聘求职
            id.startsWith("cat_8_job") -> listOf(
                AttributeSchema(key = "education", label = "学历要求", type = FieldType.SINGLE_CHOICE, options = listOf("不限", "高中及以下", "大专", "本科及以上"), isRequired = true),
                AttributeSchema(key = "experience", label = "经验要求", type = FieldType.SINGLE_CHOICE, options = listOf("不限", "应届生", "1年以内", "1-3年", "3-5年", "5年以上"), isRequired = true),
                AttributeSchema(key = "benefits", label = "福利待遇 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("五险一金", "包吃住", "周末双休", "年底双薪", "餐补", "交通补助", "定期体检"))
            )

            // 2. 家政/护理
            id.startsWith("cat_2_service") -> listOf(
                AttributeSchema(key = "serviceType", label = "服务类型", type = FieldType.SINGLE_CHOICE, options = listOf("日常保洁", "深度保洁", "家电清洗", "开荒保洁", "月嫂/育儿", "陪护看护"), isRequired = true),
                AttributeSchema(key = "billingMode", label = "计费方式", type = FieldType.SINGLE_CHOICE, options = listOf("按小时", "按平米", "按项目/次", "面议"), isRequired = true),
                AttributeSchema(key = "serviceScope", label = "包含服务 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("自带工具", "专业消毒", "损坏包赔", "免费上门", "试用满意再付费"))
            )

            // 3. 便民维修
            id.startsWith("cat_3_repair") -> listOf(
                AttributeSchema(key = "repairCategory", label = "维修类别", type = FieldType.SINGLE_CHOICE, options = listOf("水电管道", "家电维修", "开锁换锁", "房屋修缮", "数码/电脑"), isRequired = true),
                AttributeSchema(key = "responseSpeed", label = "上门速度", type = FieldType.SINGLE_CHOICE, options = listOf("30分钟急修", "1小时内", "预约上门", "全天候24小时")),
                AttributeSchema(key = "guarantee", label = "服务保障 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("修不好不收费", "质保90天", "正品配件", "明码标价"))
            )

            // 7. 拼车/租车
            id.startsWith("cat_7_carpool") -> listOf(
                AttributeSchema(key = "carType", label = "车型类型", type = FieldType.SINGLE_CHOICE, options = listOf("舒适轿车", "SUV/越野", "商务MPV", "大巴/中巴", "小货车/卡车"), isRequired = true),
                AttributeSchema(key = "seatsAvailable", label = "余座/可乘人数", type = FieldType.NUMBER_INPUT, unit = "人"),
                AttributeSchema(key = "tripFeatures", label = "行程亮点 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("全程高速", "大后备箱", "可带宠物", "禁烟车厢", "门到门接送"))
            )

            // 1. 个人闲置
            id.startsWith("cat_1_idle") -> listOf(
                AttributeSchema(key = "condition", label = "成色", type = FieldType.SINGLE_CHOICE, options = listOf("全新", "99新/几乎全新", "95新", "85新及以下"), isRequired = true),
                AttributeSchema(key = "origin", label = "入手渠道", type = FieldType.SINGLE_CHOICE, options = listOf("国行专柜", "电商正品", "赠送/抽奖", "海淘代购")),
                AttributeSchema(key = "tradeMode", label = "交易方式 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("支持当面交易", "支持邮寄/包邮", "送货上门", "验货后付款"))
            )

            // 9. 吃喝玩乐
            id.startsWith("cat_9_life") -> listOf(
                AttributeSchema(key = "serviceType", label = "服务类型", type = FieldType.SINGLE_CHOICE, options = listOf("餐饮美食", "休闲娱乐", "丽人养生", "婚庆摄影", "农家乐/周边游"), isRequired = true),
                AttributeSchema(key = "serviceEnv", label = "服务环境 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("免费茶点", "免费停车", "独立卫浴", "双人包间", "无隐形消费")),
                AttributeSchema(key = "notice", label = "温馨提示", type = FieldType.TEXT_INPUT)
            )

            // 10. 教育培训
            id.startsWith("cat_10_edu") -> listOf(
                AttributeSchema(key = "courseType", label = "课程类型", type = FieldType.SINGLE_CHOICE, options = listOf("线下体验课", "一对一辅导", "精品小班", "全日制集训"), isRequired = true),
                AttributeSchema(key = "targetAge", label = "适合年龄段", type = FieldType.SINGLE_CHOICE, options = listOf("学前/早教", "小学", "初中/高中", "成人")),
                AttributeSchema(key = "features", label = "机构特色 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("免费试听", "名师团队", "随时退费", "地铁沿线"))
            )

            // 4. 同城生鲜
            id.startsWith("cat_4_fresh") -> listOf(
                AttributeSchema(key = "freshType", label = "商品类型", type = FieldType.SINGLE_CHOICE, options = listOf("应季鲜果", "本地绿叶菜", "散养土鸡/肉禽", "河鲜/水产", "农家干货"), isRequired = true),
                AttributeSchema(key = "packaging", label = "包装规格", type = FieldType.SINGLE_CHOICE, options = listOf("散装", "礼盒装", "真空包装", "泡沫箱冷链")),
                AttributeSchema(key = "delivery", label = "配送服务 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("同城当日达", "支持自提", "坏单包赔", "足斤足两"))
            )

            // 兜底匹配（兼容无前缀的旧版 ID 或仅靠 name 匹配）
            id.contains("house") || id.contains("rent") || id.contains("sale") || name.contains("房") -> listOf(
                AttributeSchema(key = "layout", label = "户型", type = FieldType.SINGLE_CHOICE, options = listOf("1室", "2室", "3室", "4室及以上", "单间"), isRequired = true),
                AttributeSchema(key = "area", label = "建筑面积", type = FieldType.NUMBER_INPUT, unit = "㎡", isRequired = true),
                AttributeSchema(key = "orientation", label = "房屋朝向", type = FieldType.SINGLE_CHOICE, options = listOf("南", "南北", "东", "西", "北")),
                AttributeSchema(key = "features", label = "房源特色 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("满五唯一", "近地铁", "精装修", "电梯房", "随时看房", "采光极佳", "带车位"))
            )

            id.contains("job") || id.contains("recruit") || name.contains("招聘") || name.contains("求职") -> listOf(
                AttributeSchema(key = "education", label = "学历要求", type = FieldType.SINGLE_CHOICE, options = listOf("不限", "高中及以下", "大专", "本科及以上"), isRequired = true),
                AttributeSchema(key = "experience", label = "经验要求", type = FieldType.SINGLE_CHOICE, options = listOf("不限", "应届生", "1年以内", "1-3年", "3-5年", "5年以上"), isRequired = true),
                AttributeSchema(key = "benefits", label = "福利待遇 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("五险一金", "包吃住", "周末双休", "年底双薪", "餐补", "交通补助", "定期体检"))
            )

            id.contains("service") || id.contains("cleaning") || name.contains("家政") || name.contains("护理") || name.contains("保洁") -> listOf(
                AttributeSchema(key = "serviceType", label = "服务类型", type = FieldType.SINGLE_CHOICE, options = listOf("日常保洁", "深度保洁", "家电清洗", "开荒保洁", "月嫂/育儿", "陪护看护"), isRequired = true),
                AttributeSchema(key = "billingMode", label = "计费方式", type = FieldType.SINGLE_CHOICE, options = listOf("按小时", "按平米", "按项目/次", "面议"), isRequired = true),
                AttributeSchema(key = "serviceScope", label = "包含服务 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("自带工具", "专业消毒", "损坏包赔", "免费上门", "试用满意再付费"))
            )

            id.contains("repair") || id.contains("plumbing") || name.contains("维修") || name.contains("修") -> listOf(
                AttributeSchema(key = "repairCategory", label = "维修类别", type = FieldType.SINGLE_CHOICE, options = listOf("水电管道", "家电维修", "开锁换锁", "房屋修缮", "数码/电脑"), isRequired = true),
                AttributeSchema(key = "responseSpeed", label = "上门速度", type = FieldType.SINGLE_CHOICE, options = listOf("30分钟急修", "1小时内", "预约上门", "全天候24小时")),
                AttributeSchema(key = "guarantee", label = "服务保障 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("修不好不收费", "质保90天", "正品配件", "明码标价"))
            )

            id.contains("carpool") || id.contains("car") || name.contains("拼车") || name.contains("租车") || name.contains("出行") -> listOf(
                AttributeSchema(key = "carType", label = "车型类型", type = FieldType.SINGLE_CHOICE, options = listOf("舒适轿车", "SUV/越野", "商务MPV", "大巴/中巴", "小货车/卡车"), isRequired = true),
                AttributeSchema(key = "seatsAvailable", label = "余座/可乘人数", type = FieldType.NUMBER_INPUT, unit = "人"),
                AttributeSchema(key = "tripFeatures", label = "行程亮点 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("全程高速", "大后备箱", "可带宠物", "禁烟车厢", "门到门接送"))
            )

            id.contains("idle") || id.contains("second") || name.contains("闲置") || name.contains("二手") -> listOf(
                AttributeSchema(key = "condition", label = "成色", type = FieldType.SINGLE_CHOICE, options = listOf("全新", "99新/几乎全新", "95新", "85新及以下"), isRequired = true),
                AttributeSchema(key = "origin", label = "入手渠道", type = FieldType.SINGLE_CHOICE, options = listOf("国行专柜", "电商正品", "赠送/抽奖", "海淘代购")),
                AttributeSchema(key = "tradeMode", label = "交易方式 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("支持当面交易", "支持邮寄/包邮", "送货上门", "验货后付款"))
            )

            id.contains("beauty") || name.contains("丽人") || name.contains("养生") || name.contains("美容") -> listOf(
                AttributeSchema(key = "serviceType", label = "服务类型", type = FieldType.SINGLE_CHOICE, options = listOf("面部护理", "身体SPA", "美甲美睫", "中医养生", "其他"), isRequired = true),
                AttributeSchema(key = "serviceEnv", label = "服务环境 (可多选)", type = FieldType.MULTI_CHOICE, options = listOf("免费茶点", "免费停车", "独立卫浴", "双人包间", "无隐形消费")),
                AttributeSchema(key = "notice", label = "温馨提示", type = FieldType.TEXT_INPUT)
            )

            // 默认兜底 Schema
            else -> listOf(
                AttributeSchema(
                    key = "condition",
                    label = "服务/商品成色或规格",
                    type = FieldType.SINGLE_CHOICE,
                    options = listOf("优质/全新", "良好/标准", "普通")
                ),
                AttributeSchema(
                    key = "highlights",
                    label = "核心亮点 (可多选)",
                    type = FieldType.MULTI_CHOICE,
                    options = listOf("同城送货", "支持议价", "品质保证", "售后无忧")
                )
            )
        }
    }
}
