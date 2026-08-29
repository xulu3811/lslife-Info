package com.qingyuan.lslife.feature.profile

import com.qingyuan.lslife.core.model.SignInExecuteResponse
import com.qingyuan.lslife.core.model.SignInStatusResponse
import com.qingyuan.lslife.core.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignInRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getStatus(): SignInStatusResponse {
        return withContext(Dispatchers.IO) {
            apiService.getSignInStatus().data ?: SignInStatusResponse(false, 0)
        }
    }

    suspend fun executeSignIn(): SignInExecuteResponse {
        return withContext(Dispatchers.IO) {
            apiService.executeSignIn().data ?: SignInExecuteResponse(false, 0, 0, 0)
        }
    }
}
