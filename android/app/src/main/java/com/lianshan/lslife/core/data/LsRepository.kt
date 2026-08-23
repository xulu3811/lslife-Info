package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.database.MerchantDao
import com.lianshan.lslife.core.database.MerchantEntity
import com.lianshan.lslife.core.model.*
import com.lianshan.lslife.core.network.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/** 业务数据仓库: 商家/购物车/订单/支付/发布/会员/通知/AI。 */
@Singleton
class LsRepository @Inject constructor(
    private val api: ApiService,
    private val merchantDao: MerchantDao,
    private val categoryRepository: CategoryRepository,
) {
    // 商家 (offline-first: 成功后写入本地缓存, 失败时回退缓存)
    suspend fun merchants(category: String?, q: String?, sort: String, page: Int = 1, pageSize: Int = 20): Result<MerchantPage> {
        val result = safeCall { api.merchants(category = category?.takeIf { it != "all" }, q = q?.takeIf { it.isNotBlank() }, sort = sort, page = page, pageSize = pageSize) }
        result.onSuccess { resPage ->
            if ((category == null || category == "all") && page == 1) {
                runCatching {
                    merchantDao.upsertAll(resPage.list.map { it.toEntity() })
                }
            }
        }
        if (result.isFailure && category in listOf(null, "all") && q.isNullOrBlank() && page == 1) {
            val cached = runCatching { merchantDao.getAll() }.getOrNull().orEmpty()
            if (cached.isNotEmpty()) {
                return Result.success(MerchantPage(cached.size, 1, cached.size, cached.map { it.toMerchant() }))
            }
        }
        return result
    }

    suspend fun getCachedMerchants(): List<Merchant> {
        return runCatching { merchantDao.getAll() }.getOrNull().orEmpty().map { it.toMerchant() }
    }

    suspend fun recommended() = safeCall { api.recommended() }
    suspend fun merchant(id: String) = safeCall { api.merchant(id) }

    // 首页 Banner & 矩阵展位
    suspend fun getBanners() = safeCall { api.getBanners() }
    suspend fun getHomeMatrix() = safeCall { api.getHomeMatrix() }




    // 地址
    suspend fun addresses() = safeCall { api.addresses() }
    suspend fun addAddress(body: AddressBody) = safeCall { api.addAddress(body) }
    suspend fun updateAddress(id: String, body: AddressBody) = safeCall { api.updateAddress(id, body) }
    suspend fun deleteAddress(id: String) = safeCall { api.deleteAddress(id) }

    // 发布
    suspend fun createPost(req: CreatePostRequest) = safeCall { api.createPost(req) }
    suspend fun posts(
        category: String? = null,
        publisherType: String? = null,
        listingType: String? = null,
        publisherId: String? = null,
        mine: Boolean? = null,
        q: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,
        attrFilter: Map<String, String>? = null,
        postType: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ) = safeCall { 
        val attrJson = attrFilter?.takeIf { it.isNotEmpty() }?.let { map ->
            kotlinx.serialization.json.buildJsonObject {
                map.forEach { (k, v) -> put(k, kotlinx.serialization.json.JsonPrimitive(v)) }
            }.toString()
        }
        api.posts(category, publisherType, listingType, publisherId, mine, q, minPrice, maxPrice, sortBy, attrJson, postType, page, pageSize) 
    }

    suspend fun quota() = safeCall { api.quota() }
    suspend fun getDiscoverPosts(categoryId: String?) = safeCall { api.getDiscoverPosts(categoryId) }
    suspend fun post(id: String) = safeCall { api.post(id) }
    suspend fun toggleFavorite(id: String) = safeCall { api.toggleFavorite(id) }
    suspend fun getFavorites(page: Int = 1, pageSize: Int = 50) = safeCall { api.getFavorites(page, pageSize) }
    suspend fun getFootprints(page: Int = 1, pageSize: Int = 50) = safeCall { api.getFootprints(page, pageSize) }
    suspend fun clearFootprints() = safeCall { api.clearFootprints() }
    
    suspend fun toggleFollow(userId: String) = safeCall { api.toggleFollow(userId) }
    suspend fun getFollowers(userId: String, page: Int = 1, pageSize: Int = 50) = safeCall { api.getFollowers(userId, page, pageSize) }
    suspend fun getFollowing(userId: String, page: Int = 1, pageSize: Int = 50) = safeCall { api.getFollowing(userId, page, pageSize) }

    suspend fun updatePost(id: String, req: CreatePostRequest) = safeCall { api.updatePost(id, req) }
    suspend fun updatePostStatus(id: String, status: String) = safeCall { api.updatePostStatus(id, mapOf("status" to status)) }
    suspend fun auditAdminPost(id: String, action: String, note: String?) = safeCall { 
        val body = mutableMapOf("action" to action)
        if (note != null) body["note"] = note
        api.auditAdminPost(id, body)
    }
    suspend fun deletePost(id: String) = safeCall { api.deletePost(id) }

    // 会员
    suspend fun plans() = safeCall { api.plans() }
    suspend fun subscribe(tier: String) = safeCall { api.subscribe(SubscribeRequest(tier)) }

    // 通知
    suspend fun notifications() = safeCall { api.notifications() }
    suspend fun readAllNotifications() = safeCall { api.readAllNotifications() }

    // 分类树与动态 Schema (从 CategoryRepository SSOT 获取)
    suspend fun categoryTree(forceRefresh: Boolean = false) = categoryRepository.getCategoryTree(forceRefresh)
    suspend fun categorySchema(id: String) = categoryRepository.getCategorySchema(id)

    // AI
    suspend fun aiRecommend(prompt: String) = safeCall { api.aiRecommend(AiRequest(prompt)) }
    suspend fun aiGenerateDescription(
        title: String? = null,
        categoryId: String? = null,
        draft: String? = null,
        schema: List<DynamicField> = emptyList(),
    ): Result<AiGenerateDescResponse> = safeCall { api.aiGenerateDescription(AiGenerateDescRequest(title, categoryId, draft, schema)) }

    // 上传
    suspend fun uploadImage(part: okhttp3.MultipartBody.Part) = safeCall { api.uploadImage(part) }
    suspend fun uploadChatImage(part: okhttp3.MultipartBody.Part) = safeCall { api.uploadChatImage(part) }
    suspend fun uploadImagesBatch(parts: List<okhttp3.MultipartBody.Part>) = safeCall { api.uploadImagesBatch(parts) }
    suspend fun uploadAudio(part: okhttp3.MultipartBody.Part) = safeCall { api.uploadAudio(part) }

    // 商家认证
    suspend fun getMerchantCertifyStatus(): Result<MerchantCertification?> = safeCall { api.merchantCertifyStatus() }
    suspend fun submitMerchantCertification(req: MerchantCertifyRequest) = safeCall { api.submitMerchantCertification(req) }
    suspend fun performBusinessOcr(url: String) = safeCall { api.performBusinessOcr(OcrRequest(url)) }
    
    suspend fun getUserPublicProfile(userId: String): Result<PublicUserResponse> = safeCall { api.getUserPublicProfile(userId) }
    
    // 推广中心
    suspend fun buyPromotion(req: PromotionBuyRequest) = safeCall { api.buyPromotion(req) }
    suspend fun getMyPromotions(): Result<List<PromotionTask>> = safeCall { api.getMyPromotions() }
    suspend fun getPromotionStats(): Result<PromotionStatsResponse> = safeCall { api.getPromotionStats() }
    suspend fun rechargeCards(req: RechargeCardsRequest): Result<Unit> = safeCall { api.rechargeCards(req) }
    suspend fun getMyPostsForPromotion(): Result<PostPage> = safeCall { api.posts(mine = true, page = 1, pageSize = 50) }
}

private fun Merchant.toEntity() = MerchantEntity(
    id = id, name = name, rating = rating, distance = distance, sales = sales, avgPrice = avgPrice,
    tags = tags.joinToString("|"), deliveryFee = deliveryFee, deliveryTime = deliveryTime,
    logo = logo, category = category, cachedAt = System.currentTimeMillis(),
)

private fun MerchantEntity.toMerchant() = Merchant(
    id = id, name = name, rating = rating, distance = distance, sales = sales, avgPrice = avgPrice,
    tags = if (tags.isBlank()) emptyList() else tags.split("|"), deliveryFee = deliveryFee, deliveryTime = deliveryTime,
    logo = logo, banner = logo, isFood = false, category = category, latitude = 0.0, longitude = 0.0,
    description = "", address = "", phone = "",
)
