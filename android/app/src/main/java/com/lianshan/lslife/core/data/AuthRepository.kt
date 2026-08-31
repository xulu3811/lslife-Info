package com.qingyuan.lslife.core.data

import com.qingyuan.lslife.core.model.LoginResult
import com.qingyuan.lslife.core.model.User
import com.qingyuan.lslife.core.network.ApiService
import com.qingyuan.lslife.core.network.LoginRequest
import com.qingyuan.lslife.core.network.RealNameRequest
import com.qingyuan.lslife.core.network.RegisterRequest
import com.qingyuan.lslife.core.network.safeCall
import com.qingyuan.lslife.core.network.ResetPasswordRequest
import com.qingyuan.lslife.core.network.SendEmailCodeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    val isLoggedIn: Flow<Boolean> = tokenStore.tokenFlow.map { it != null }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun cachedMe(): User? = _currentUser.value

    suspend fun register(phone: String, email: String, password: String, nickname: String? = null): Result<LoginResult> =
        safeCall { api.register(RegisterRequest(phone, email, password, nickname)) }
            .onSuccess { tokenStore.save(it.token) }

    suspend fun sendEmailCode(email: String): Result<Map<String, String>> =
        safeCall { api.sendEmailCode(SendEmailCodeRequest(email)) }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Map<String, String>> =
        safeCall { api.resetPassword(ResetPasswordRequest(email, code, newPassword)) }

    suspend fun login(phone: String, password: String): Result<LoginResult> =
        safeCall { api.login(LoginRequest(phone, password)) }
            .onSuccess { tokenStore.save(it.token) }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Map<String, Boolean>> =
        safeCall { api.changePassword(com.qingyuan.lslife.core.network.ChangePasswordRequest(oldPassword, newPassword)) }

    suspend fun me(): Result<User> = safeCall { api.me() }.onSuccess { _currentUser.value = it }

    suspend fun performOcr(imageUrl: String): Result<com.qingyuan.lslife.core.network.OcrResponse> =
        safeCall { api.performOcr(com.qingyuan.lslife.core.network.OcrRequest(imageUrl)) }

    suspend fun realName(name: String, idCard: String, idCardFrontImage: String?, idCardBackImage: String?): Result<User> =
        safeCall { api.realName(RealNameRequest(name, idCard, idCardFrontImage, idCardBackImage)) }

    suspend fun updateProfile(nickname: String?, avatar: String?): Result<User> =
        safeCall { api.updateProfile(com.qingyuan.lslife.core.model.ProfileUpdateRequest(nickname, avatar)) }

    suspend fun logout() {
        _currentUser.value = null
        tokenStore.clear()
    }
}
