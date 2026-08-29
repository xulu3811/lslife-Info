package com.qingyuan.lslife.core.network

import com.qingyuan.lslife.core.data.TokenStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskControlInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {
    @OptIn(DelicateCoroutinesApi::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 403) {
            val bodyString = response.peekBody(2048).string()
            if (bodyString.contains("BANNED") || bodyString.contains("banned") || bodyString.contains("封禁")) {
                GlobalScope.launch {
                    tokenStore.clear()
                }
            }
        }
        return response
    }
}
