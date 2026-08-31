package com.qingyuan.lslife.core.network

import com.qingyuan.lslife.core.model.FlexibleJsonObjectSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginRequest(val phone: String, val password: String)

@Serializable
data class RegisterRequest(val phone: String, val email: String, val password: String, val nickname: String? = null)

@Serializable
data class SendEmailCodeRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)

@Serializable
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

@Serializable
data class RealNameRequest(
    val realName: String, 
    val idCard: String,
    val idCardFrontImage: String? = null,
    val idCardBackImage: String? = null
)

@Serializable
data class OcrRequest(val imageUrl: String)

@Serializable
data class OcrResponse(
    val realName: String,
    val idCard: String
)

@Serializable
data class BusinessOcrResponse(
    val legalPerson: String,
    val creditCode: String
)

@Serializable
data class DeliveryAddressBody(val name: String, val phone: String, val address: String)

@Serializable
data class OrderItemRequest(val productId: String? = null, val postId: String? = null, val quantity: Int)

@Serializable
data class CreateOrderRequest(
    val merchantId: String? = null,
    val sellerId: String? = null,
    val items: List<OrderItemRequest>,
    val deliveryAddress: DeliveryAddressBody,
    val deliveryMethod: String? = null,
    val pickupTime: String? = null,
)

@Serializable
data class CreatePaymentRequest(val orderId: String, val channel: String)

@Serializable
data class MockConfirmRequest(val orderNo: String)

@Serializable
data class AddressBody(
    val name: String,
    val phone: String,
    val tag: String = "家",
    val address: String,
    val isDefault: Boolean = false,
)

@Serializable
data class PublishRequest(
    val title: String,
    val description: String,
    val price: Double? = null,
    val contactPhone: String? = null,
    val category: String,
    val images: List<String> = emptyList(),
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val town: String? = null,
    val streetAddress: String? = null,
    val postType: String = "CLASSIFIED",
    val attributes: JsonObject = JsonObject(emptyMap())
)

// ================= Promotion (推广中心) =================
@Serializable
data class PromotionBuyRequest(
    val postId: String,
    val type: String,
    val days: Int? = 1
)

@Serializable
data class RechargeCardsRequest(
    val quantity: Int
)

@Serializable
data class PromotionStatsResponse(
    val totalViews: Int,
    val contactViews: Int,
    val totalFavorites: Int,
    val beatRate: Int,
    val bumpCards: Int
)

@Serializable
data class PromotionTask(
    val id: String,
    val type: String,
    val status: String,
    val startTime: String,
    val endTime: String,
    val post: PostSnippet? = null
)

@Serializable
data class PostSnippet(
    val id: String,
    val title: String,
    val images: String
)

@Serializable
data class CreatePostRequest(
    val category: String,
    val title: String? = null,
    val description: String,
    val price: Double? = null,
    val images: List<String> = emptyList(),
    val imageHashes: List<String> = emptyList(),
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    val postType: String = "COMMERCE",
    val tradeMode: String = "INFO",
    val contactPhone: String? = null,
    val useUrgentTag: Boolean = false,

    val attributes: @Serializable(with = FlexibleJsonObjectSerializer::class) JsonObject = JsonObject(emptyMap()),
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val town: String? = null,
    val streetAddress: String? = null,
    val linkedCommerceId: String? = null,
    val topic: String? = null,
)

@Serializable
data class SubscribeRequest(val tier: String)

@Serializable
data class AiRequest(val prompt: String)

@Serializable
data class AiGenerateDescRequest(
    val title: String? = null,
    val categoryId: String? = null,
    val draft: String? = null,
    val schema: List<com.qingyuan.lslife.core.model.DynamicField> = emptyList(),
)

@Serializable
data class AiGenerateDescResponse(
    val title: String,
    val description: String,
    val attributes: @Serializable(with = FlexibleJsonObjectSerializer::class) JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class UploadResult(val url: String)

@Serializable
data class BatchUploadResult(val urls: List<String>)

@Serializable
data class RechargeRequest(
    val amount: Double,
    val type: String = "cash",
    val channel: String = "wechat",
)

@Serializable
data class MerchantCertifyRequest(
    val certType: String,
    val storeName: String,
    val categoryId: String,
    val contactName: String,
    val contactPhone: String,
    val businessLicenseUrl: String? = null,
    val storePhotos: List<String> = emptyList(),
    val isDraft: Boolean = false,
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val town: String? = null,
    val streetAddress: String? = null
)

@Serializable
data class MerchantCertification(
    val id: String,
    val userId: String,
    val status: String,
    val certType: String,
    val storeName: String,
    val categoryId: String,
    val contactName: String,
    val contactPhone: String,
    val businessLicenseUrl: String? = null,
    val storePhotos: List<String> = emptyList(),
    val rejectReason: String? = null,
    val expireAt: String? = null,
    val lastConfirmedAt: String? = null
)

@Serializable
data class PublicUserResponse(
    val id: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val authLabel: String? = null,
    val isMerchant: Boolean = false,
    val identityType: String? = null,
    val createdAt: String? = null,
    val isFollowing: Boolean = false,
    val businessLicenseUrl: String? = null,
    val storeName: String? = null,
    val certType: String? = null
)

@Serializable
data class AdminUserStatusRequest(
    val status: String
)

@Serializable
data class AdminReportResolveRequest(
    val action: String
)

@Serializable
data class ProfileReviewUser(
    val id: String,
    val phone: String,
    val nickname: String,
    val avatar: String? = null,
    val pendingNickname: String? = null,
    val pendingAvatar: String? = null,
    val profileReviewNote: String? = null,
    val updatedAt: String
)

@Serializable
data class KycUser(
    val id: String,
    val phone: String,
    val nickname: String,
    val realName: String? = null,
    val idCardHash: String? = null,
    val realNameStatus: String,
    val updatedAt: String
)

@Serializable
data class FriendRequestPayload(
    val friendId: String,
    val message: String? = null
)

@Serializable
data class FriendHandleRequest(
    val requestId: String,
    val action: String
)

@Serializable
data class FriendListResponse(
    val friends: List<PublicUserResponse>
)

@Serializable
data class FriendRequestItem(
    val id: String,
    val sender: PublicUserResponse,
    val message: String? = null,
    val createdAt: String
)

@Serializable
data class FriendRequestsResponse(
    val requests: List<FriendRequestItem>
)

@Serializable
data class FollowUserItem(
    val id: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val identityType: String? = null,
    val authLabel: String? = null
)

@Serializable
data class FollowListResponse(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<FollowUserItem>
)

@Serializable
data class GovernanceUserDto(
    val id: String,
    val phone: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val status: String? = null,
    val role: String? = null,
    val realNameStatus: String? = null
)

@Serializable
data class GovernancePostUserDto(
    val id: String,
    val nickname: String? = null,
    val phone: String? = null
)

@Serializable
data class GovernancePostDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val user: GovernancePostUserDto? = null
)
