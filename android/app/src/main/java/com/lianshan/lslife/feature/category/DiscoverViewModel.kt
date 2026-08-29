package com.qingyuan.lslife.feature.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.CategoryRepository
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.model.CategoryNode
import com.qingyuan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryGroup(
    val category: CategoryNode,
    val subCategories: List<CategoryNode>
)

data class DiscoverUiState(
    val categoryTree: List<CategoryNode> = emptyList(),
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val posts: List<Post> = emptyList(),
    val sort: String = "default",
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state

    init {
        observeCategoryTree()
        load(showFullLoading = true, page = 1)
    }

    private fun observeCategoryTree() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree()
        }
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                val topLevel = tree.filter { it.parentId == null }
                val groups = topLevel.map { topCat ->
                    val mappedChildren = tree.find { it.id == topCat.id }?.children?.map { subCat ->
                        var newIcon = subCat.iconUrl
                        
                        val mappedIcon = when (subCat.name) {
                            // 1. 个人闲置
                            "数码3C", "数码 3C" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_3c_v2"
                            "服饰箱包" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_clothing_v3"
                            "家电/家具", "日用/家电" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_home_appliances_v2"
                            "美妆个护" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_beauty_v2"
                            "母婴儿童" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_baby"
                            "运动/代步", "运动 & 交通工具", "运动&交通工具" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_sports_mobility_v2"
                            "文娱用品", "文娱爱好" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_entertainment_v2"
                            "其他闲置", "其它" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_other_idle_v2"
                            
                            // 2. 家政/护理
                            "日常保洁" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_daily_cleaning_v4"
                            "深度/开荒保洁", "深度保洁" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_deep_cleaning_v4"
                            "家电清洗" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_appliance_clean_v10"
                            "保姆/钟点工", "保姆/钟点" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_nanny_hourly_v3"
                            "月嫂/育儿" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_maternity_childcare_v3"
                            "陪护/看护", "育儿/陪护" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_caregiving_v3"
                            "搬家/货运" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_moving"

                            // 3. 便民维修
                            "水电/管道" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_plumbing_detail_v2"
                            "开锁/换锁" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_locksmith"
                            "家电维修" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_repair_v2"
                            "房屋修缮" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_renovation_v2"
                            "数码/电脑维修" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_digital_repair_v2"

                            // 4. 同城生鲜
                            "新鲜水果" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_fresh_fruit_v5"
                            "时令蔬菜" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_fresh_veg_v3"
                            "肉禽蛋品" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_fresh_meat_v3"
                            "海鲜水产" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_fresh_seafood_v3"
                            "冷冻速食", "冷藏冻货", "冻藏冻货" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_frozen_food_v3"
                            "粮油调味" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_fresh_grocery_v3"
                            "熟食卤味" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_fresh_deli_v3"

                            // 5. 房屋出租
                            "整租/合租" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_house_rent_v4"
                            "商铺/办公出租", "商铺/办公" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_office_v4"
                            "厂房/仓库出租" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_warehouse_rent_v5"
                            "日租/短租", "日租/民宿" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_short_term_rent_v4"
                            "车位出租" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_parking_rent_v4"

                            // 6. 二手房产
                            "二手房出租", "二手房出售", "二手买卖" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_house_sale_v5"
                            "商铺/写字楼出售", "商铺/写字楼" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_office_sale_v2"
                            "厂房/土地转让", "厂房/土地" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_land_factory_transfer_v3"
                            "车位租售", "车位出售" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_parking_sale_v3"

                            "新房/楼盘推荐" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_new_property_v2"

                            // 7. 拼车/租车
                            "车找人" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_carpool_v2"
                            "人找车" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_passenger_v3"
                            "顺路捎货", "捎带货" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_freight_v2"
                            "搬家/同城货车", "搬家/货运", "搬家/同城货运" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_moving_van_v6"
                            "汽车租赁", "轿车 / SUV 租赁", "轿车/SUV租赁" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_car_rental_v3"
                            "婚车/车队租赁", "婚车租赁" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_wedding_car_v2"
                            "大巴/工程车出租", "大巴商务车租赁", "货车/工程车租赁" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_bus_construction_rent_v6"

                            // 8. 招聘求职
                            "全职招聘", "全职直招" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_full_time_job_v2"
                            "兼职/日结", "日结/临工" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_part_time_daily_v2"
                            "餐饮/服务行业", "餐饮/服务业", "餐饮/酒店", "餐饮/客房" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_catering_service_job_v2"
                            "普工/技工", "普工/制造" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_factory_worker_job_v2"
                            "销售/客服" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_job_sales_v2"
                            "司机/仓储", "跑腿/代办" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_job_driver_v2"
                            "行政/财务", "人事/财务" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_admin_finance_job_v2"

                            // 9. 本地生活
                            "餐饮美食", "餐饮类" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_gourmet_dining_v3"
                            "休闲娱乐", "娱乐休闲类" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_recreation_entertainment_v2"
                            "丽人养生" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_beauty_spa_v2"
                            "婚庆摄影" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_wedding_photography_v2"
                            "农家乐/周边游", "农家乐/周边" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_agritourism_travel_v4"

                            // 10. 教育培训
                            "学科辅导", "中小学辅导" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_academic_tutoring"
                            "艺术/体育", "艺术" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_arts_sports_v2"
                            "职业/考证", "职业技能" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_vocational_certification_v2"
                            "驾校报名", "驾校招考" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_driving_school_v3"
                            "少儿早教" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_early_education_v2"
                            "AI/科技编程", "AI编程/科学" -> "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_ai_coding_v2"
                            
                            else -> null
                        }

                        if (mappedIcon != null) {
                            newIcon = mappedIcon
                        } else if (newIcon.isNullOrEmpty() || newIcon.contains("3d_flat") || newIcon.contains("unsplash")) {
                            newIcon = "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_other_idle"
                        }

                        subCat.copy(
                            iconUrl = newIcon,
                            type = com.qingyuan.lslife.core.model.CategoryType.SERVICE
                        )
                    } ?: emptyList()

                    CategoryGroup(
                        category = topCat,
                        subCategories = mappedChildren
                    )
                }
                
                val sortedGroups = groups.sortedBy { if (it.category.name == "个人闲置") 1 else 0 }
                _state.update { it.copy(categoryTree = tree, categoryGroups = sortedGroups) }
            }
        }
    }

    fun onSort(s: String) {
        if (_state.value.sort == s) return
        _state.update { it.copy(sort = s) }
        load(showFullLoading = true, page = 1)
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        load(showFullLoading = false, page = 1)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _state.update { it.copy(loadingMore = true) }
        load(showFullLoading = false, page = s.page + 1)
    }

    fun load(showFullLoading: Boolean = true, page: Int = 1) {
        val s = _state.value
        viewModelScope.launch {
            if (showFullLoading) _state.update { it.copy(loading = true, error = null) }
            
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(posts = emptyList()) }
            }

            val sortParam = if (s.sort == "default") null else s.sort

            repo.posts(
                category = null, 
                publisherType = null,
                listingType = null,
                mine = false, 
                q = null, 
                sortBy = sortParam, 
                minPrice = null,
                maxPrice = null,
                attrFilter = null,
                page = page, 
                pageSize = 20
            )
                .onSuccess { resPage ->
                    val list = resPage.list
                    _state.update {
                        val newPosts = if (page == 1) list else it.posts + list
                        val hasMore = resPage.page * resPage.pageSize < resPage.total
                        it.copy(
                            loading = false, loadingMore = false, refreshing = false,
                            posts = newPosts, error = null,
                            page = page, hasMore = hasMore
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, loadingMore = false, refreshing = false, error = e.message ?: "加载失败") }
                }
        }
    }
}
