package com.lianshan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.MembershipPlan
import com.lianshan.lslife.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val plans: List<MembershipPlan> = emptyList(),
    val unread: Int = 0,
    val message: String? = null,
    val loggedOut: Boolean = false,
    val realNameSubmitting: Boolean = false,
    val merchantCertStatus: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.me()
                .onSuccess { u -> _state.update { it.copy(loading = false, user = u) } }
                .onFailure { e -> _state.update { it.copy(loading = false, message = "获取用户信息失败") } }
            repo.plans().onSuccess { p -> _state.update { it.copy(plans = p) } }
            repo.notifications().onSuccess { n -> _state.update { it.copy(unread = n.unread) } }
            repo.getMerchantCertifyStatus().onSuccess { cert -> 
                _state.update { it.copy(merchantCertStatus = cert?.status) }
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
        handheldUri: android.net.Uri, 
        context: android.content.Context
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _state.update { it.copy(realNameSubmitting = true, message = "正在上传照片...") }
            try {
                val parts = listOf(
                    createMultipart(frontUri, context, "front.jpg"),
                    createMultipart(backUri, context, "back.jpg"),
                    createMultipart(handheldUri, context, "handheld.jpg")
                )
                
                val res = repo.uploadImagesBatch(parts.filterNotNull())
                if (res.isFailure || res.getOrNull()?.urls?.size != 3) {
                    _state.update { it.copy(realNameSubmitting = false, message = "图片上传失败，请重试") }
                    return@launch
                }
                val urls = res.getOrNull()!!.urls

                _state.update { it.copy(message = "正在提交认证资料...") }
                authRepository.realName(name, idCard, urls[0], urls[1], urls[2])
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
    
    private fun createMultipart(uri: android.net.Uri, context: android.content.Context, filename: String): okhttp3.MultipartBody.Part? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = java.io.File(context.cacheDir, filename)
        tempFile.outputStream().use { out -> inputStream.use { it.copyTo(out) } }
        val reqFile = okhttp3.RequestBody.create(
            "image/jpeg".toMediaTypeOrNull(), tempFile
        )
        return okhttp3.MultipartBody.Part.createFormData("images", filename, reqFile)
    }
}
