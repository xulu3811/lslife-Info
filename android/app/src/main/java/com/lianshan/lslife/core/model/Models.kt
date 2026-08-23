package com.lianshan.lslife.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
enum class TradeMode {
    INFO_PUBLISH,
    INFO
}

@Serializable
enum class PostType {
    CLASSIFIED
}

@Serializable
data class ApiEnvelope<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
)

@Serializable
data class Banner(
    val id: String,
    val title: String,
    val imageUrl: String,
    val linkUrl: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

@Serializable
data class HomeMatrixData(
    val featuredMerchants: List<Post> = emptyList(),
    val specialOffers: List<Post> = emptyList(),
)

@Serializable
data class User(
    val id: String,
    val phone: String,
    val nickname: String,
    val role: String = "USER",
    val avatar: String? = null,
    val membershipTier: String = "free",
    val realNameStatus: String = "none",
    val realName: String? = null,
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val favoritesCount: Int = 0,
    val footprintsCount: Int = 0,
    val identityType: String = "NORMAL",
    val creditScore: Int = 100,
    val status: String = "active",
    val profileReviewStatus: String = "APPROVED",
    val pendingNickname: String? = null,
    val pendingAvatar: String? = null,
    val privileges: JsonElement? = null
) {
    val isMerchant: Boolean
        get() = identityType == "MERCHANT_VERIFIED"
}

@Serializable
data class AdminUserListItem(
    val id: String,
    val phone: String,
    val nickname: String,
    val avatar: String? = null,
    val status: String = "active",
    val createdAt: String
)

@Serializable
data class AdminUserListResponse(
    val items: List<AdminUserListItem>,
    val total: Int,
    val page: Int,
    val limit: Int
)

@Serializable
data class Reporter(
    val id: String,
    val nickname: String,
    val avatar: String? = null,
    val phone: String
)

@Serializable
data class Report(
    val id: String,
    val reporterId: String,
    val targetType: String,
    val targetId: String,
    val reason: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val reporter: Reporter? = null,
    val targetTitle: String? = null
)

@Serializable
data class LoginResult(val token: String, val user: User)



@Serializable
data class Merchant(
    val id: String,
    val name: String,
    val rating: Double,
    val distance: Double,
    val sales: Int,
    val avgPrice: Double,
    val tags: List<String> = emptyList(),
    val deliveryFee: Double,
    val deliveryTime: Int,
    val logo: String,
    val banner: String,
    val isFood: Boolean = false,
    val category: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val address: String,
    val phone: String,
    val ownerId: String? = null,
)

@Serializable
data class MerchantPage(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<Merchant>,
)



@Serializable
data class Address(
    val id: String,
    val name: String,
    val phone: String,
    val tag: String,
    val address: String,
    val isDefault: Boolean,
)

@Serializable
data class Post(
    val id: String,
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    val category: String,
    val title: String,
    val description: String,
    val price: Double? = null,
    val contactPhone: String? = null,
    @kotlinx.serialization.SerialName("tradeMode")
    val _tradeMode: TradeMode = TradeMode.INFO_PUBLISH,
    val postType: PostType = PostType.CLASSIFIED,
    val likeCount: Int = 0,
    val isSponsored: Boolean = false,
    val isFavorite: Boolean = false,
    val isFollowing: Boolean = false,
    val images: List<String> = emptyList(),
    val status: String,
    val locationName: String? = null,
    val attributes: @Serializable(with = FlexibleJsonObjectSerializer::class) JsonObject = JsonObject(emptyMap()),
    val reviewNote: String? = null,
    val createdAt: String,
    val topic: String? = null,
    val linkedCommerceId: String? = null,
    val sellerType: String = "INDIVIDUAL",
    val marketingHook: String? = null,
    val isFeatured: Boolean = false,
    val isUrgent: Boolean = false,
    val distanceText: String? = null,
    val activeTimeText: String? = null,
    val badgeText: String? = null,
    val user: PostUser? = null,
    val merchant: PostMerchant? = null,
) {
    val tradeMode: TradeMode
        get() = com.lianshan.lslife.feature.publish.getEffectiveTradeMode(_tradeMode, category)
}


@Serializable
data class PostMerchant(
    val name: String,
    val logo: String,
    val status: String? = null,
)

@Serializable
data class PostUser(
    val id: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val isMerchant: Boolean = false,
    val authLabel: String = "认证个人用户",
    val merchantId: String? = null,
    val identityType: String? = null
)

@Serializable
data class PostPage(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<Post>,
    val aggregations: Map<String, Map<String, Int>> = emptyMap(),
)

@Serializable
data class FavoriteToggleResponse(
    val isFavorite: Boolean,
    val likeCount: Int
)

@Serializable
data class Quota(val used: Int, val limit: Int, val tier: String, val remaining: Int? = null)

@Serializable
data class MembershipPlan(
    val tier: String,
    val name: String,
    val price: Double,
    val period: String,
    val benefits: List<String>,
)

@Serializable
data class NotificationItem(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val orderId: String? = null,
    val read: Boolean,
    val createdAt: String,
)

@Serializable
data class NotificationResult(val list: List<NotificationItem>, val unread: Int)

@Serializable
data class AiRecommendation(
    val merchantId: String,
    val itemId: String,
    val name: String,
    val price: Double,
)

@Serializable
data class AiReply(val reply: String, val recommendations: List<AiRecommendation> = emptyList())

@Serializable
data class ProfileUpdateRequest(
    val nickname: String? = null,
    val avatar: String? = null,
)

@Serializable
data class ChatUser(
    val id: String,
    val nickname: String,
    val avatar: String? = null,
)

@Serializable
data class ChatSession(
    val id: String,
    val targetUser: ChatUser? = null,
    val lastMessage: String? = null,
    val unread: Int = 0,
    val updatedAt: String,
)

@Serializable
data class WalletTransaction(
    val id: String,
    val type: String, // points | cash
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val bizType: String,
    val description: String? = null,
    val createdAt: String,
)

@Serializable
data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
)

@Serializable
data class UserWalletResponse(
    val coinBalance: Int,
    val totalRecharged: Double
)

@Serializable
data class WalletLog(
    val id: String,
    val userId: String,
    val amount: Int,
    val balanceAfter: Int,
    val tradeType: String,
    val relatedBizId: String? = null,
    val createdAt: String
)

@Serializable
data class WalletLogPage(
    val items: List<WalletLog> = emptyList(),
    val pagination: Pagination
)

@Serializable
data class RechargePackage(
    val id: Int,
    val price: Double,
    val coinsAmount: Int,
    val bonusCoins: Int = 0,
    val isActive: Boolean = true
)

@Serializable
data class ConsumeRequest(
    val amount: Int,
    val tradeType: String,
    val relatedBizId: String? = null
)

@Serializable
data class DynamicField(
    val key: String,
    val label: String,
    val fieldType: String, // SINGLE_CHOICE | MULTI_CHOICE | TEXT | NUMBER | BOOLEAN
    val required: Boolean = false,
    val placeholder: String? = null,
    val options: List<String> = emptyList(),
)

@Serializable
enum class CategoryType {
    PRODUCT, SERVICE
}

@Serializable
data class CategoryNode(
    val id: String,
    val name: String,
    val icon: String? = null,
    val iconUrl: String? = null,
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val isLeaf: Boolean = false,
    val isActive: Boolean = true,
    val isHot: Boolean = false,
    val tradeMode: TradeMode = TradeMode.INFO_PUBLISH,
    val type: CategoryType = CategoryType.PRODUCT,
    val attributeSchema: List<DynamicField> = emptyList(),
    val children: List<CategoryNode> = emptyList(),
)

@Serializable
data class CategorySchemaResponse(
    val categoryId: String,
    val name: String,
    val isLeaf: Boolean = false,
    val attributeSchema: List<DynamicField> = emptyList(),
)

@Serializable
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val senderId: String,
    val type: String = "text",
    val content: String,
    val isRecalled: Boolean = false,
    val isEncrypted: Boolean = false,
    val evidenceHash: String? = null,
    val mediaHash: String? = null,
    val createdAt: String,
    @kotlinx.serialization.Transient val isOfflineSync: Boolean = false,
)

@Serializable
data class ChatImageUploadResult(
    val url: String,
    val mediaHash: String
)

object FlexibleJsonObjectSerializer : KSerializer<JsonObject> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FlexibleJsonObject")

    override fun serialize(encoder: Encoder, value: JsonObject) {
        JsonObject.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): JsonObject {
        val input = decoder as? JsonDecoder ?: return JsonObject(emptyMap())
        return try {
            val element = input.decodeJsonElement()
            when {
                element is JsonObject -> element
                element is JsonPrimitive && element.isString -> {
                    val str = element.content.trim()
                    if (str.startsWith("{") && str.endsWith("}")) {
                        Json.parseToJsonElement(str).jsonObject
                    } else {
                        JsonObject(emptyMap())
                    }
                }
                else -> JsonObject(emptyMap())
            }
        } catch (e: Exception) {
            JsonObject(emptyMap())
        }
    }
}


@Serializable
data class SignInStatusResponse(
    @SerialName("is_signed_today") val isSignedToday: Boolean,
    @SerialName("continuous_days") val continuousDays: Int
)

@Serializable
data class SignInExecuteResponse(
    val success: Boolean,
    @SerialName("reward_coins") val rewardCoins: Int,
    @SerialName("current_continuous_days") val currentContinuousDays: Int,
    @SerialName("balance_after") val balanceAfter: Int? = null
)

// ============ OTA 版本升级 ============
@Serializable
data class AppVersionInfo(
    val id: String,
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val isForced: Boolean,
    val isActive: Boolean = true,
    val fileSize: Long? = null,
    val md5: String? = null,
    val createdAt: String? = null,
)
