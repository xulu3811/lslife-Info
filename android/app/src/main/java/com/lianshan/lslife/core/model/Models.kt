package com.lianshan.lslife.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
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
    C2C_IDLE,
    O2O_STORE,
    SERVICE_ORDER,
    // 兼容历史老数据
    INFO,
    COMMERCE
}

@Serializable
data class ApiEnvelope<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
)

@Serializable
data class User(
    val id: String,
    val phone: String,
    val nickname: String,
    val avatar: String? = null,
    val membershipTier: String = "free",
    val realNameStatus: String = "none",
    val realName: String? = null,
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val followersCount: Int = 0,
    val favoritesCount: Int = 0,
    val footprintsCount: Int = 0,
    val identityType: String = "NORMAL",
    val creditScore: Int = 100
)

@Serializable
data class LoginResult(val token: String, val user: User)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val originalPrice: Double? = null,
    val desc: String,
    val sales: Int = 0,
    val image: String,
    val category: String,
    val merchant: Merchant? = null,
)

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
    val items: List<Product> = emptyList(),
)

@Serializable
data class MerchantPage(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<Merchant>,
)

@Serializable
data class CartEntry(
    val id: String,
    val quantity: Int,
    val merchantId: String? = null,
    val sellerId: String? = null,
    val product: Product? = null,
    val post: Post? = null,
)

@Serializable
data class OrderItem(
    val id: String,
    val productId: String? = null,
    val postId: String? = null,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String,
)

@Serializable
data class Rider(
    val name: String,
    val phone: String,
    val avatar: String,
    val lat: Double,
    val lng: Double,
)

@Serializable
data class Delivery(
    val status: String,
    val progress: Int,
    val secondsRemaining: Int,
    val rider: Rider,
)

@Serializable
data class Order(
    val id: String,
    val orderNo: String,
    val merchantId: String? = null,
    val sellerId: String? = null,
    val merchantName: String? = null,
    val merchantLogo: String? = null,
    val itemsTotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String,
    val deliveryMethod: String = "DELIVERY",
    val pickupTime: String? = null,
    val deliveryName: String,
    val deliveryPhone: String,
    val deliveryAddress: String,
    val createdAt: String,
    val items: List<OrderItem> = emptyList(),
    val delivery: Delivery? = null,
)

@Serializable
data class PaymentCreateResult(
    val paymentId: String? = null,
    val prepayPayload: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val paid: Boolean = false,
    val orderId: String? = null,
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
    val originalPrice: Double? = null,
    val contactPhone: String? = null,
    val stock: Int = 0,
    val deliveryType: String = "SELF_PICKUP",
    @kotlinx.serialization.SerialName("tradeMode")
    val _tradeMode: TradeMode = TradeMode.COMMERCE, // Default to COMMERCE for backward compatibility with old backend data
    val images: List<String> = emptyList(),
    val status: String,
    val locationName: String? = null,
    val attributes: @Serializable(with = FlexibleJsonObjectSerializer::class) JsonObject = JsonObject(emptyMap()),
    val reviewNote: String? = null,
    val createdAt: String,
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
    val fieldType: String, // SELECT | TEXT | NUMBER | DATE
    val required: Boolean = false,
    val placeholder: String? = null,
    val options: List<String> = emptyList(),
)

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
    val tradeMode: TradeMode = TradeMode.COMMERCE,
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
    val createdAt: String,
    @kotlinx.serialization.Transient val isOfflineSync: Boolean = false,
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

