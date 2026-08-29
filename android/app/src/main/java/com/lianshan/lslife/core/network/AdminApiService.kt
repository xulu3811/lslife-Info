package com.qingyuan.lslife.core.network

import com.qingyuan.lslife.core.model.*
import retrofit2.http.*
import kotlinx.serialization.json.JsonObject

interface AdminApiService {

    @GET("admin/dashboard")
    suspend fun getDashboard(): ApiEnvelope<AdminDashboardData>

    @GET("admin/kyc")
    suspend fun getKycUsers(@Query("status") status: String = "pending"): ApiEnvelope<List<AdminKycUser>>

    @POST("admin/kyc/{id}/audit")
    suspend fun auditKycUser(@Path("id") id: String, @Body request: AdminActionRequest): ApiEnvelope<JsonObject>

    @GET("admin/posts")
    suspend fun getPosts(@Query("status") status: String? = null): ApiEnvelope<List<AdminPost>>

    @POST("admin/posts/{id}/audit")
    suspend fun auditPost(@Path("id") id: String, @Body request: AdminActionRequest): ApiEnvelope<JsonObject>

    @GET("admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("keyword") keyword: String = ""
    ): ApiEnvelope<AdminUsersResponse>

    @POST("admin/users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: String, @Body request: AdminStatusRequest): ApiEnvelope<AdminUser>

    @POST("admin/security/force-logout-all")
    suspend fun forceLogoutAll(): ApiEnvelope<JsonObject>
}
