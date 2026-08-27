package com.lianshan.lslife.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AdminDashboardData(
    val newUsers: Int = 0,
    val revenue: Double = 0.0,
    val pendingReviews: Int = 0,
    val pendingKyc: Int = 0,
    val pendingMerchantCerts: Int = 0
)

@Serializable
data class AdminKycUser(
    val id: String,
    val phone: String,
    val nickname: String?,
    val realName: String?,
    val idCardHash: String?,
    val idCardFrontImage: String?,
    val idCardBackImage: String?,
    val idCardHandheldImage: String?,
    val realNameStatus: String,
    val updatedAt: String
)

@Serializable
data class AdminPostUser(
    val nickname: String?,
    val phone: String?
)

@Serializable
data class AdminPost(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val reviewNote: String?,
    val images: List<String> = emptyList(),
    val attributes: JsonElement? = null,
    val user: AdminPostUser?,
    val createdAt: String
)

@Serializable
data class AdminUser(
    val id: String,
    val phone: String,
    val nickname: String?,
    val avatar: String?,
    val status: String,
    val realNameStatus: String,
    val membershipTier: String,
    val walletBalance: Double = 0.0,
    val createdAt: String
)

@Serializable
data class AdminUsersResponse(
    val items: List<AdminUser>,
    val total: Int,
    val page: Int,
    val limit: Int
)

@Serializable
data class AdminCategory(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
    val parentId: String? = null,
    val sortOrder: Int,
    val isLeaf: Boolean,
    val isActive: Boolean,
    val attributeSchema: String
)

@Serializable
data class AdminMerchant(
    val id: String,
    val name: String,
    val logo: String?,
    val phone: String,
    val status: String,
    val createdAt: String
)

@Serializable
data class AdminMerchantsResponse(
    val list: List<AdminMerchant>
)

@Serializable
data class AdminActionRequest(
    val action: String,
    val note: String? = null,
    val reason: String? = null
)

@Serializable
data class AdminStatusRequest(
    val status: String
)
