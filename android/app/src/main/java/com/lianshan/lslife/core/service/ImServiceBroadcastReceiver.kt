package com.lianshan.lslife.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.lianshan.lslife.core.data.TokenStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ImServiceBroadcastReceiver : BroadcastReceiver() {
    
    @Inject lateinit var tokenStore: TokenStore
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ConnectivityManager.CONNECTIVITY_ACTION ||
            action == Intent.ACTION_USER_PRESENT ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_SCREEN_ON ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val keepAlive = tokenStore.keepAliveFlow.first()
                    if (keepAlive) {
                        LsLifeImService.start(context)
                    }
                } catch (ignored: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
