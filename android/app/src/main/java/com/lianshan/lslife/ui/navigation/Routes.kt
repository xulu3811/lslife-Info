package com.lianshan.lslife.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val PUBLISH = "publish?postId={postId}&categoryId={categoryId}"
    const val MY_POSTS = "my_posts"
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
    const val ADDRESS_LIST = "address_list"
    const val ADDRESS_EDIT = "address_edit?addressId={addressId}"
    const val MESSAGE_LIST = "message_list"
    const val CATEGORY = "category?primaryId={primaryId}"
    const val CATEGORY_DETAIL = "category_detail_route/{categoryId}"
    const val REAL_NAME_AGREEMENT = "real_name_agreement"
    const val REAL_NAME_AUTH = "real_name_auth/{signature}"
    const val CROP_AVATAR = "crop_avatar"
    const val CHAT = "chat/{sessionId}/{targetUserId}/{targetName}?initPostId={initPostId}"
    const val SEARCH = "search"
    const val POST_DETAIL = "post_detail/{postId}"

    fun merchant(id: String) = "merchant/$id"
    fun publicProfile(userId: String) = "public_profile/$userId"
    fun postDetail(id: String) = "post_detail/$id"
    fun addressEdit(addressId: String? = null) =
        if (addressId.isNullOrBlank()) "address_edit?addressId=" else "address_edit?addressId=$addressId"
    fun cropAvatar() = "crop_avatar"
    fun chat(sessionId: String, targetUserId: String, targetName: String, initPostId: String? = null) = 
        "chat/$sessionId/$targetUserId/$targetName" + if (!initPostId.isNullOrBlank()) "?initPostId=$initPostId" else ""
    fun publish(postId: String? = null, categoryId: String? = null) = 
        if (postId.isNullOrBlank() && categoryId.isNullOrBlank()) "publish" 
        else "publish?postId=${postId ?: ""}&categoryId=${categoryId ?: ""}"
    fun realNameAuth(signature: String) = "real_name_auth/$signature"
    fun categoryWithArg(primaryId: String) = "category?primaryId=$primaryId"
    fun categoryDetail(categoryId: String) = "category_detail_route/${java.net.URLEncoder.encode(categoryId, "UTF-8")}"
    const val MOMENT_PUBLISH = "moment_publish?topic={topic}&momentType={momentType}"
    fun momentPublish(topic: String? = null, momentType: String? = null): String {
        val encodedTopic = topic?.takeIf { it.isNotBlank() }?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""
        val type = momentType ?: ""
        return "moment_publish?topic=$encodedTopic&momentType=$type"
    }
}
