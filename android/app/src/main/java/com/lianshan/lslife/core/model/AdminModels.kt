package com.qingyuan.lslife.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AdminDashboardData(
    val totalUsers: Int = 0,
    val totalMembers: Int = 0,
    val kycPercentage: String = "0%",
    val verifiedMerchants: Int = 0,
    val serverStorageStatus: String = "未知",
    val totalRecharge: Double = 0.0
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

@Serializable
data class ServerMonitorRam(
    val total: String,
    val used: String,
    val percent: Float
)

@Serializable
data class ServerMonitorCpu(
    val cores: Int,
    val loadAvg: Float,
    val percent: Float
)

@Serializable
data class ServerMonitorDisk(
    val total: String,
    val used: String,
    val percent: Int
)

@Serializable
data class ServerMonitorPm2(
    val name: String,
    val status: String,
    val memory: String,
    val cpu: Float,
    val restarts: Int,
    val uptime: Long
)

@Serializable
data class ServerMonitorData(
    val ram: ServerMonitorRam,
    val cpu: ServerMonitorCpu,
    val disk: ServerMonitorDisk,
    val pm2: List<ServerMonitorPm2>
)
