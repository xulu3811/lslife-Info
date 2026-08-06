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

    // 购物车
    @GET("cart")
    suspend fun cart(): ApiEnvelope<List<CartEntry>>

    @POST("cart")
    suspend fun upsertCart(@Body body: CartUpsertRequest): ApiEnvelope<Map<String, kotlinx.serialization.json.JsonElement>>

    @DELETE("cart")
    suspend fun clearCart(@Query("merchantId") merchantId: String? = null): ApiEnvelope<Map<String, Boolean>>

    // 订单
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): ApiEnvelope<Order>

    @GET("orders")
    suspend fun orders(): ApiEnvelope<List<Order>>

    @GET("orders/{id}")
    suspend fun order(@Path("id") id: String): ApiEnvelope<Order>

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): ApiEnvelope<Order>

    // 支付
    @POST("payments/create")
    suspend fun createPayment(@Body body: CreatePaymentRequest): ApiEnvelope<PaymentCreateResult>

    @POST("payments/mock-confirm")
    suspend fun mockConfirm(@Body body: MockConfirmRequest): ApiEnvelope<PaymentCreateResult>

    @POST("payments/recharge")
    suspend fun recharge(@Body body: RechargeRequest): ApiEnvelope<PaymentCreateResult>

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

    @GET("posts/discover")
    suspend fun getDiscoverPosts(@Query("categoryId") categoryId: String? = null): ApiEnvelope<List<DiscoverSection>>

    @GET("posts/quota")
    suspend fun quota(): ApiEnvelope<Quota>

    @GET("posts/{id}")
    suspend fun post(@Path("id") id: String): ApiEnvelope<Post>

    @POST("posts/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: String): ApiEnvelope<FavoriteToggleResponse>

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
    @POST("upload/batch")
    suspend fun uploadImagesBatch(@Part images: List<okhttp3.MultipartBody.Part>): ApiEnvelope<BatchUploadResult>

    @Multipart
    @POST("upload/audio")
    suspend fun uploadAudio(@Part audio: okhttp3.MultipartBody.Part): ApiEnvelope<UploadResult>

    // 聊天
    @GET("chat/sessions")
    suspend fun chatSessions(): ApiEnvelope<List<ChatSession>>

    @GET("chat/sessions/{id}/messages")
    suspend fun chatMessages(@Path("id") id: String): ApiEnvelope<List<ChatMessage>>

    @POST("chat/sessions/{id}/messages/{msgId}/recall")
    suspend fun recallMessage(@Path("id") sessionId: String, @Path("msgId") msgId: String): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    // 商家认证
    @GET("merchants/certify/status")
    suspend fun merchantCertifyStatus(): ApiEnvelope<MerchantCertification?>

    @POST("merchants/certify")
    suspend fun submitMerchantCertification(@Body body: MerchantCertifyRequest): ApiEnvelope<MerchantCertification>

    @GET("users/{userId}/public")
    suspend fun getUserPublicProfile(@Path("userId") userId: String): ApiEnvelope<PublicUserResponse>

    // 签到相关
    @GET("user/signin/status")
    suspend fun getSignInStatus(): ApiEnvelope<SignInStatusResponse>

    @POST("user/signin/execute")
    suspend fun executeSignIn(): ApiEnvelope<SignInExecuteResponse>
}
