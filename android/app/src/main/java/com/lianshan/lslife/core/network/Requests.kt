package com.lianshan.lslife.core.network

import com.lianshan.lslife.core.model.FlexibleJsonObjectSerializer
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
    val idCardBackImage: String? = null,
    val idCardHandheldImage: String? = null
)

@Serializable
data class CartUpsertRequest(val productId: String? = null, val postId: String? = null, val quantity: Int)

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
data class CreatePostRequest(
    val category: String,
    val title: String? = null,
    val description: String,
    val price: Double? = null,
    val images: List<String> = emptyList(),
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    val postType: String = "COMMERCE",
    val tradeMode: String = "INFO",
    val contactPhone: String? = null,
    val originalPrice: Double? = null,
    val stock: Int = 0,
    val deliveryType: String = "SELF_PICKUP",
    val attributes: @Serializable(with = FlexibleJsonObjectSerializer::class) JsonObject = JsonObject(emptyMap()),
    val locationName: String? = null,
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
    val schema: List<com.lianshan.lslife.core.model.DynamicField> = emptyList(),
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
    val storePhotos: List<String> = emptyList()
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
    val rejectReason: String? = null
)

@Serializable
data class PublicUserResponse(
    val id: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val authLabel: String? = null,
    val isMerchant: Boolean = false,
    val identityType: String? = null,
    val createdAt: String? = null
)
