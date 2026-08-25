package com.lianshan.lslife.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val PUBLISH = "publish?postId={postId}&categoryId={categoryId}"
    const val MY_POSTS = "my_posts"
    const val FOLLOW_LIST = "follow_list"
    const val FAVORITES = "favorites"
    const val FOOTPRINTS = "footprints"
    const val PROFILE = "profile"
    const val WALLET = "wallet"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val MERCHANT = "merchant/{merchantId}"
    const val PERSONAL_INFO = "personal_info"
    const val EDIT_PROFILE = "edit_profile"
    const val MEMBERSHIP = "membership"
    const val MERCHANT_CERTIFY = "merchant_certify"
    const val PUBLIC_PROFILE = "public_profile/{userId}"

    const val MESSAGE_LIST = "message_list"
    const val CATEGORY = "category?primaryId={primaryId}"
    const val CATEGORY_DETAIL = "category_detail_route/{categoryId}"
    const val REAL_NAME_AUTH = "real_name_auth"
    const val CROP_AVATAR = "crop_avatar"
    const val CHAT = "chat/{sessionId}/{targetUserId}/{targetName}?initPostId={initPostId}"
    const val SEARCH = "search"
    const val POST_DETAIL = "post_detail/{postId}?mode={mode}&reportId={reportId}"
    const val ADMIN_REVIEW_LIST = "admin_review_list"
    const val ADMIN_USER_LIST = "admin_user_list"
    const val ADMIN_REPORT_LIST = "admin_report_list"
    const val ADMIN_APPROVAL_DASHBOARD = "admin_approval_dashboard"
    const val ADMIN_PROFILE_REVIEW_LIST = "admin_profile_review_list"
    const val ADMIN_KYC_REVIEW_LIST = "admin_kyc_review_list"
    const val ADMIN_MERCHANT_CERT_REVIEW_LIST = "admin_merchant_cert_review_list"
    const val ADMIN_GOVERNANCE_CENTER = "admin_governance_center"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    fun merchant(id: String) = "merchant/$id"
    fun publicProfile(userId: String) = "public_profile/$userId"
    fun favorites() = "favorites"
    fun footprints() = "footprints"
    fun postDetail(id: String, mode: String? = null, reportId: String? = null): String {
        val base = "post_detail/$id"
        val params = mutableListOf<String>()
        if (!mode.isNullOrBlank()) params.add("mode=$mode")
        if (!reportId.isNullOrBlank()) params.add("reportId=$reportId")
        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
    }

    fun cropAvatar() = "crop_avatar"
    fun chat(sessionId: String, targetUserId: String, targetName: String, initPostId: String? = null) = 
        "chat/$sessionId/$targetUserId/$targetName" + if (!initPostId.isNullOrBlank()) "?initPostId=$initPostId" else ""
    fun publish(postId: String? = null, categoryId: String? = null) = 
        if (postId.isNullOrBlank() && categoryId.isNullOrBlank()) "publish" 
        else "publish?postId=${postId ?: ""}&categoryId=${categoryId ?: ""}"
    fun realNameAuth() = "real_name_auth"
    fun categoryWithArg(primaryId: String) = "category?primaryId=$primaryId"
    fun categoryDetail(categoryId: String) = "category_detail_route/${java.net.URLEncoder.encode(categoryId, "UTF-8")}"
}
