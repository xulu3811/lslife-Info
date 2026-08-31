package com.qingyuan.lslife.feature.profile

import kotlinx.serialization.json.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.AuthRepository
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.model.MembershipPlan
import com.qingyuan.lslife.core.model.SignInStatusResponse
import com.qingyuan.lslife.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import javax.inject.Inject

import com.qingyuan.lslife.core.network.ApiService

data class ProfileUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val plans: List<MembershipPlan> = emptyList(),
    val unread: Int = 0,
    val pendingReviews: Int = 0,
    val message: String? = null,
    val loggedOut: Boolean = false,
    val realNameSubmitting: Boolean = false,
    val merchantCertStatus: String? = null,
    val signInStatus: SignInStatusResponse? = null,
    val isSigningIn: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val repo: LsRepository,
    private val signInRepository: SignInRepository,
    private val api: ApiService,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    fun load(isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent || _state.value.user == null) {
                _state.update { it.copy(loading = true) }
            }
            authRepository.me()
                .onSuccess { u -> 
                    _state.update { it.copy(loading = false) } 
                    if (u.role == "ADMIN" || u.role == "SUPERADMIN") {
                        try {
                            val res = api.getAdminDashboard()
                            if (res.code == 0) {
                                val pendingPosts = res.data?.get("pendingReviews")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch(e: Exception) { null } } ?: 0
                                val pendingProfile = res.data?.get("pendingProfileReviews")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch(e: Exception) { null } } ?: 0
                                val pendingKyc = res.data?.get("pendingKyc")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch(e: Exception) { null } } ?: 0
                                val pendingMerchant = res.data?.get("pendingMerchantCerts")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch(e: Exception) { null } } ?: 0
                                val pendingCount = pendingPosts + pendingProfile + pendingKyc + pendingMerchant
                                _state.update { it.copy(pendingReviews = pendingCount) }
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
                .onFailure { e -> _state.update { it.copy(loading = false, message = "获取用户信息失败") } }
            repo.plans().onSuccess { p -> _state.update { it.copy(plans = p) } }
            repo.notifications().onSuccess { n -> _state.update { it.copy(unread = n.unread) } }
            repo.getMerchantCertifyStatus().onSuccess { cert -> 
                _state.update { it.copy(merchantCertStatus = cert?.status) }
            }
            try {
                val status = signInRepository.getStatus()
                _state.update { it.copy(signInStatus = status) }
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    fun confirmMerchantActive() {
        viewModelScope.launch {
            val res = repo.confirmActive()
            if (res.isSuccess) {
                _state.update { it.copy(message = "打卡成功，店铺状态已恢复") }
                load(isSilent = true)
            } else {
                _state.update { it.copy(message = res.exceptionOrNull()?.message ?: "打卡失败") }
            }
        }
    }

    fun subscribe(tier: String) {
        viewModelScope.launch {
            repo.subscribe(tier)
                .onSuccess { _state.update { it.copy(message = "会员开通成功") }; load() }
                .onFailure { e -> _state.update { it.copy(message = e.message ?: "开通失败") } }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(loggedOut = true) }
        }
    }

    fun submitRealName(
        name: String, 
        idCard: String, 
        frontUri: android.net.Uri, 
        backUri: android.net.Uri, 
        context: android.content.Context
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _state.update { it.copy(realNameSubmitting = true, message = "正在上传照片...") }
            try {
                val parts = listOf(
                    createMultipart(frontUri, context, "front.jpg"),
                    createMultipart(backUri, context, "back.jpg")
                )
                
                val res = repo.uploadImagesBatch(parts.filterNotNull())
                if (res.isFailure || res.getOrNull()?.urls?.size != 2) {
                    _state.update { it.copy(realNameSubmitting = false, message = "图片上传失败，请重试") }
                    return@launch
                }
                val urls = res.getOrNull()!!.urls

                _state.update { it.copy(message = "正在提交认证资料...") }
                authRepository.realName(name, idCard, urls[0], urls[1])
                    .onSuccess { 
                        _state.update { it.copy(realNameSubmitting = false, message = "认证资料已提交，请等待审核") }
                        load()
                    }
                    .onFailure { e ->
                        _state.update { it.copy(realNameSubmitting = false, message = e.message ?: "提交失败") }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(realNameSubmitting = false, message = "处理失败: ${e.message}") }
            }
        }
    }
    
    fun performOcr(frontUri: android.net.Uri, context: android.content.Context, onResult: (String, String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _state.update { it.copy(message = "正在识别身份信息...") }
            try {
                val part = createMultipart(frontUri, context, "front_ocr.jpg") ?: return@launch
                val uploadRes = repo.uploadImagesBatch(listOf(part))
                if (uploadRes.isSuccess && uploadRes.getOrNull()?.urls?.isNotEmpty() == true) {
                    val url = uploadRes.getOrNull()!!.urls[0]
                    authRepository.performOcr(url).onSuccess {
                        onResult(it.realName, it.idCard)
                        _state.update { s -> s.copy(message = "识别成功") }
                    }.onFailure { e ->
                        _state.update { s -> s.copy(message = "识别失败: ${e.message}") }
                    }
                } else {
                    _state.update { it.copy(message = "图片上传失败") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(message = "识别出错: ${e.message}") }
            }
        }
    }
    
    private fun createMultipart(uri: android.net.Uri, context: android.content.Context, filename: String): okhttp3.MultipartBody.Part? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = java.io.File(context.cacheDir, filename)
        tempFile.outputStream().use { out -> inputStream.use { it.copyTo(out) } }
        
        // 压缩图片至2MB以内，避免超过服务端5MB限制
        val compressedBytes = com.qingyuan.lslife.feature.publish.ImageCompressor.compress(context, tempFile.absolutePath, 2 * 1024 * 1024, 1920)
        
        val reqFile = okhttp3.RequestBody.create(
            "image/jpeg".toMediaTypeOrNull(), compressedBytes
        )
        return okhttp3.MultipartBody.Part.createFormData("images", filename, reqFile)
    }

    fun executeSignIn() {
        viewModelScope.launch {
            _state.update { it.copy(isSigningIn = true) }
            try {
                val res = signInRepository.executeSignIn()
                if (res.success) {
                    _state.update { 
                        it.copy(
                            isSigningIn = false, 
                            message = "签到成功！获得 ${res.rewardCoins} 猫币",
                            signInStatus = SignInStatusResponse(true, res.currentContinuousDays),
                            user = it.user?.copy(walletBalance = res.balanceAfter?.toDouble() ?: it.user.walletBalance)
                        ) 
                    }
                } else {
                    _state.update { it.copy(isSigningIn = false, message = "签到失败") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSigningIn = false, message = e.message ?: "签到失败") }
            }
        }
    }
}
