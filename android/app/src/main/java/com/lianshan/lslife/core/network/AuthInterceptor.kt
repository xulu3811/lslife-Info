package com.qingyuan.lslife.core.network

import android.content.Context
import com.qingyuan.lslife.core.data.TokenStore
import com.qingyuan.lslife.core.utils.DeviceFingerprintUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** 自动为请求附加 Bearer token 和设备指纹 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    @ApplicationContext private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.cachedToken
        val deviceId = DeviceFingerprintUtil.getDeviceId(context)
        val deviceRisk = DeviceFingerprintUtil.getDeviceRisk()

        val requestBuilder = chain.request().newBuilder()
            .addHeader("X-Device-Id", deviceId)
            .addHeader("X-Device-Risk", deviceRisk)

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
