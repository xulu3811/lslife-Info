package com.lianshan.lslife.feature.wallet

import com.lianshan.lslife.core.model.ConsumeRequest
import com.lianshan.lslife.core.model.RechargePackage
import com.lianshan.lslife.core.model.UserWalletResponse
import com.lianshan.lslife.core.model.WalletLogPage
import com.lianshan.lslife.core.network.ApiService
import javax.inject.Inject

class WalletRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getWalletInfo(): UserWalletResponse {
        val response = apiService.getWalletInfo()
        if (response.code != 0) {
            throw Exception(response.message ?: "获取钱包信息失败")
        }
        return response.data!!
    }

    suspend fun getPackages(): List<RechargePackage> {
        val response = apiService.getRechargePackages()
        if (response.code != 0) {
            throw Exception(response.message ?: "获取套餐失败")
        }
        return response.data ?: emptyList()
    }

    suspend fun getLogs(page: Int, limit: Int): WalletLogPage {
        val response = apiService.getWalletLogs(page, limit)
        if (response.code != 0) {
            throw Exception(response.message ?: "获取账单明细失败")
        }
        return response.data!!
    }

    suspend fun consumeCoins(amount: Int, tradeType: String, relatedBizId: String? = null) {
        val response = apiService.consumeCoins(ConsumeRequest(amount, tradeType, relatedBizId))
        if (response.code != 0) {
            throw Exception(response.message ?: "扣费失败")
        }
    }

    suspend fun recharge(packageId: Int) {
        val response = apiService.rechargeWallet(mapOf("packageId" to packageId))
        if (response.code != 0) {
            throw Exception(response.message ?: "充值失败")
        }
    }
}
