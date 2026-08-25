package com.lianshan.lslife.core.network

import com.lianshan.lslife.core.model.*
import retrofit2.http.*

/** 后端 REST 接口, 对应 backend/src/modules。统一响应包裹在 ApiEnvelope。 */
interface ApiService {

    @GET("health")
    suspend fun health(): ApiEnvelope<Map<String, String>>

    // 分类树与动态 Schema
    @GET("categories/tree")
    suspend fun getCategoryTree(): ApiEnvelope<List<CategoryNode>>

    @GET("home/banners")
    suspend fun getBanners(): ApiEnvelope<List<Banner>>

    @GET("home/matrix")
    suspend fun getHomeMatrix(): ApiEnvelope<HomeMatrixData>

    @GET("categories/{id}/schema")
    suspend fun getCategorySchema(@Path("id") id: String): ApiEnvelope<CategorySchemaResponse>

    // 鉴权（手机号+密码；短信暂未开通）
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiEnvelope<LoginResult>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope<LoginResult>

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ApiEnvelope<Map<String, Boolean>>

    @POST("auth/forgot-password/code")
    suspend fun sendEmailCode(@Body body: SendEmailCodeRequest): ApiEnvelope<Map<String, String>>

    @POST("auth/forgot-password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ApiEnvelope<Map<String, String>>

    @GET("auth/me")
    suspend fun me(): ApiEnvelope<User>

    @POST("auth/realname/ocr")
    suspend fun performOcr(@Body body: OcrRequest): ApiEnvelope<OcrResponse>

    @POST("auth/realname")
    suspend fun realName(@Body body: RealNameRequest): ApiEnvelope<User>

    @PATCH("auth/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): ApiEnvelope<User>

    // 商家
    @GET("merchants")
    suspend fun merchants(
        @Query("category") category: String? = null,
        @Query("q") q: String? = null,
        @Query("sort") sort: String = "default",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiEnvelope<MerchantPage>

    @GET("merchants/recommended")
    suspend fun recommended(): ApiEnvelope<List<Merchant>>

    @GET("merchants/{id}")
    suspend fun merchant(@Path("id") id: String): ApiEnvelope<Merchant>



    // 钱包
    @GET("wallet/info")
    suspend fun getWalletInfo(): ApiEnvelope<UserWalletResponse>

    @GET("wallet/logs")
    suspend fun getWalletLogs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<WalletLogPage>

    @GET("wallet/packages")
    suspend fun getRechargePackages(): ApiEnvelope<List<RechargePackage>>

    @POST("wallet/consume")
    suspend fun consumeCoins(@Body body: ConsumeRequest): ApiEnvelope<Any>

    @POST("wallet/recharge")
    suspend fun rechargeWallet(@Body body: Map<String, Int>): ApiEnvelope<Any>

    // 收货地址
    @GET("addresses")
    suspend fun addresses(): ApiEnvelope<List<Address>>

    @POST("addresses")
    suspend fun addAddress(@Body body: AddressBody): ApiEnvelope<Address>

    @PUT("addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body body: AddressBody): ApiEnvelope<Address>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): ApiEnvelope<Map<String, Boolean>>

    // 同城发布
    @POST("posts")
    suspend fun createPost(@Body body: CreatePostRequest): ApiEnvelope<Post>

    @GET("posts")
    suspend fun posts(
        @Query("category") category: String? = null,
        @Query("publisherType") publisherType: String? = null,
        @Query("listingType") listingType: String? = null,
        @Query("publisherId") publisherId: String? = null,
        @Query("mine") mine: Boolean? = null,
        @Query("q") q: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("attrFilter") attrFilter: String? = null,
        @Query("postType") postType: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiEnvelope<PostPage>

    @GET("dynamics")
    suspend fun getDynamics(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null
    ): ApiEnvelope<PostPage>

    @GET("posts/discover")
    suspend fun getDiscoverPosts(@Query("categoryId") categoryId: String? = null): ApiEnvelope<List<DiscoverSection>>

    @GET("posts/quota")
    suspend fun quota(): ApiEnvelope<Quota>

    @GET("posts/{id}")
    suspend fun post(@Path("id") id: String): ApiEnvelope<Post>

    @POST("posts/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: String): ApiEnvelope<FavoriteToggleResponse>

    @GET("posts/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): ApiEnvelope<PostPage>

    // ================= Promotion (推广中心) =================
    @POST("promotions/buy")
    suspend fun buyPromotion(@Body request: PromotionBuyRequest): ApiEnvelope<Any>

    @GET("promotions/my")
    suspend fun getMyPromotions(): ApiEnvelope<List<PromotionTask>>

    @GET("promotions/stats")
    suspend fun getPromotionStats(): ApiEnvelope<PromotionStatsResponse>

    @POST("promotions/recharge_cards")
    suspend fun rechargeCards(@Body req: RechargeCardsRequest): ApiEnvelope<Unit>

    @GET("posts/footprints")
    suspend fun getFootprints(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): ApiEnvelope<PostPage>

    @DELETE("posts/footprints")
    suspend fun clearFootprints(): ApiEnvelope<Any?>

    @POST("users/{id}/follow")
    suspend fun toggleFollow(@Path("id") userId: String): ApiEnvelope<Map<String, Boolean>>

    @GET("users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): ApiEnvelope<FollowListResponse>

    @GET("users/{id}/following")
    suspend fun getFollowing(
        @Path("id") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): ApiEnvelope<FollowListResponse>

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body body: CreatePostRequest): ApiEnvelope<Post>

    @PUT("posts/{id}/status")
    suspend fun updatePostStatus(@Path("id") id: String, @Body body: Map<String, String>): ApiEnvelope<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): ApiEnvelope<Map<String, String>>

    // 会员
    @GET("membership/plans")
    suspend fun plans(): ApiEnvelope<List<MembershipPlan>>

    @POST("membership/subscribe")
    suspend fun subscribe(@Body body: SubscribeRequest): ApiEnvelope<Map<String, kotlinx.serialization.json.JsonElement>>

    // 通知
    @GET("notifications")
    suspend fun notifications(): ApiEnvelope<NotificationResult>

    @POST("notifications/read-all")
    suspend fun readAllNotifications(): ApiEnvelope<Map<String, Boolean>>

    // AI 助手
    @POST("ai/recommend")
    suspend fun aiRecommend(@Body body: AiRequest): ApiEnvelope<AiReply>

    @POST("ai/generate-description")
    suspend fun aiGenerateDescription(@Body body: AiGenerateDescRequest): ApiEnvelope<AiGenerateDescResponse>

    // 上传
    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part image: okhttp3.MultipartBody.Part): ApiEnvelope<UploadResult>

    @Multipart
    @POST("upload/upload-image")
    suspend fun uploadChatImage(@Part image: okhttp3.MultipartBody.Part): ApiEnvelope<ChatImageUploadResult>

    @Multipart
    @POST("upload/batch")
    suspend fun uploadImagesBatch(@Part images: List<okhttp3.MultipartBody.Part>): ApiEnvelope<BatchUploadResult>

    @Multipart
    @POST("upload/audio")
    suspend fun uploadAudio(@Part audio: okhttp3.MultipartBody.Part): ApiEnvelope<ChatImageUploadResult>

    // 聊天
    @GET("chat/sessions")
    suspend fun chatSessions(): ApiEnvelope<List<ChatSession>>

    @GET("chat/sessions/{id}/messages")
    suspend fun chatMessages(@Path("id") id: String): ApiEnvelope<List<ChatMessage>>

    @DELETE("chat/sessions/{id}")
    suspend fun deleteChatSession(@Path("id") id: String): ApiEnvelope<Unit>

    @POST("chat/sessions/{id}/messages/{msgId}/recall")
    suspend fun recallMessage(@Path("id") sessionId: String, @Path("msgId") msgId: String): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    // 好友
    @POST("friend/request")
    suspend fun sendFriendRequest(@Body body: FriendRequestPayload): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @POST("friend/handle")
    suspend fun handleFriendRequest(@Body body: FriendHandleRequest): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("friend/list")
    suspend fun getFriendList(): ApiEnvelope<FriendListResponse>

    @GET("friend/requests")
    suspend fun getFriendRequests(): ApiEnvelope<FriendRequestsResponse>

    // 商家认证
    @GET("merchants/certify/status")
    suspend fun merchantCertifyStatus(): ApiEnvelope<MerchantCertification?>

    @POST("merchants/certify")
    suspend fun submitMerchantCertification(@Body body: MerchantCertifyRequest): ApiEnvelope<MerchantCertification>
    
    @POST("merchants/certify/ocr")
    suspend fun performBusinessOcr(@Body body: OcrRequest): ApiEnvelope<BusinessOcrResponse>

    @GET("users/{userId}/public")
    suspend fun getUserPublicProfile(@Path("userId") userId: String): ApiEnvelope<PublicUserResponse>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): ApiEnvelope<List<PublicUserResponse>>

    // 签到相关
    @GET("user/signin/status")
    suspend fun getSignInStatus(): ApiEnvelope<SignInStatusResponse>

    @POST("user/signin/execute")
    suspend fun executeSignIn(): ApiEnvelope<SignInExecuteResponse>

    // Admin
    @GET("admin/users")
    suspend fun getAdminUsers(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("keyword") keyword: String?
    ): ApiEnvelope<AdminUserListResponse>

    @POST("admin/users/{userId}/status")
    suspend fun updateAdminUserStatus(
        @Path("userId") userId: String,
        @Body body: AdminUserStatusRequest
    ): ApiEnvelope<User>

    @GET("admin/reports")
    suspend fun getAdminReports(
        @Query("status") status: String
    ): ApiEnvelope<List<Report>>

    @POST("admin/reports/{reportId}/resolve")
    suspend fun resolveAdminReport(
        @Path("reportId") reportId: String,
        @Body body: AdminReportResolveRequest
    ): ApiEnvelope<Report>

    @GET("admin/posts")
    suspend fun getAdminPosts(
        @Query("status") status: String?
    ): ApiEnvelope<List<Post>>

    @POST("admin/posts/{id}/audit")
    suspend fun auditAdminPost(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("admin/users/profile-reviews")
    suspend fun getAdminProfileReviews(): ApiEnvelope<List<ProfileReviewUser>>

    @POST("admin/users/{userId}/audit-profile")
    suspend fun auditAdminProfileReview(
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("admin/kyc")
    suspend fun getAdminKyc(
        @Query("status") status: String = "pending"
    ): ApiEnvelope<List<KycUser>>

    @POST("admin/kyc/{id}/audit")
    suspend fun auditAdminKyc(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("admin/merchants/certify")
    suspend fun getAdminMerchantCerts(
        @Query("status") status: String = "PENDING"
    ): ApiEnvelope<List<MerchantCertification>>

    @POST("admin/merchants/certify/{id}/audit")
    suspend fun auditAdminMerchantCert(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("admin/dashboard")
    suspend fun getAdminDashboard(): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    // OTA 版本检查（公开接口，无需鉴权）
    @GET("app/version")
    suspend fun getLatestAppVersion(): ApiEnvelope<AppVersionInfo>

    @POST("admin/governance/ban-user")
    suspend fun banUser(
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @POST("admin/governance/revoke-post")
    suspend fun revokePost(
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @POST("admin/governance/revoke-kyc")
    suspend fun revokeKyc(
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @POST("admin/governance/revoke-merchant")
    suspend fun revokeMerchant(
        @Body body: Map<String, String>
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("admin/governance/search-users")
    suspend fun searchGovernanceUsers(
        @Query("keyword") keyword: String
    ): ApiEnvelope<List<GovernanceUserDto>>

    @GET("admin/governance/search-posts")
    suspend fun searchGovernancePosts(
        @Query("keyword") keyword: String
    ): ApiEnvelope<List<GovernancePostDto>>
}
