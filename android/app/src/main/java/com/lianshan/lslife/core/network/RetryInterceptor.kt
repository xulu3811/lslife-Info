package com.lianshan.lslife.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * 弱网自动重试拦截器。
 * 捕获 IOException (如 SocketTimeoutException, ConnectException 等)
 * 并在指数退避延迟后重试最多 [maxRetries] 次。
 */
class RetryInterceptor @Inject constructor() : Interceptor {
    private val maxRetries = 2
    private val baseDelayMs = 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var error: IOException? = null
        var retryCount = 0

        while (retryCount <= maxRetries) {
            try {
                response = chain.proceed(request)
                // 若服务端返回成功或客户端侧非IO异常，跳出重试
                if (response.isSuccessful) {
                    return response
                } else {
                    // 若返回 5xx 服务端错误，可以考虑重试，这里暂不处理 HTTP 状态码，仅处理底层断网
                    return response
                }
            } catch (e: IOException) {
                error = e
                retryCount++
                if (retryCount <= maxRetries) {
                    // 指数退避延迟：1s, 2s
                    try {
                        Thread.sleep(baseDelayMs * (1L shl (retryCount - 1)))
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw e
                    }
                }
            }
        }
        
        throw error ?: IOException("Unknown network error")
    }
}
