package com.qingyuan.lslife.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.model.RechargePackage
import com.qingyuan.lslife.core.model.WalletLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val coinBalance: Int = 0,
    val packages: List<RechargePackage> = emptyList(),
    val selectedPackageId: Int? = null,
    val logs: List<WalletLog> = emptyList(),
    val isLoadingLogs: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val defaultPackages = listOf(
        RechargePackage(id = 1, price = 10.0, coinsAmount = 10, bonusCoins = 0),
        RechargePackage(id = 2, price = 30.0, coinsAmount = 30, bonusCoins = 0),
        RechargePackage(id = 3, price = 48.0, coinsAmount = 50, bonusCoins = 0),
        RechargePackage(id = 4, price = 95.0, coinsAmount = 100, bonusCoins = 10),
        RechargePackage(id = 5, price = 188.0, coinsAmount = 200, bonusCoins = 25),
        RechargePackage(id = 6, price = 450.0, coinsAmount = 500, bonusCoins = 75),
    )

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWalletData()
    }

    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val walletInfo = runCatching { walletRepository.getWalletInfo() }.getOrNull()
                val pkgList = runCatching { walletRepository.getPackages() }.getOrNull()

                val finalPackages = if (!pkgList.isNullOrEmpty()) pkgList else defaultPackages

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        coinBalance = walletInfo?.coinBalance ?: 0,
                        packages = finalPackages,
                        selectedPackageId = finalPackages.getOrNull(3)?.id ?: finalPackages.firstOrNull()?.id // 默认选中高性价比档位
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        packages = defaultPackages,
                        selectedPackageId = defaultPackages[3].id,
                        error = e.message ?: "加载失败"
                    ) 
                }
            }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLogs = true) }
            try {
                val logPage = walletRepository.getLogs(page = 1, limit = 50)
                _uiState.update { it.copy(isLoadingLogs = false, logs = logPage.items) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingLogs = false) }
            }
        }
    }

    fun selectPackage(packageId: Int) {
        _uiState.update { it.copy(selectedPackageId = packageId) }
    }

    fun recharge(onSuccess: (() -> Unit)? = null) {
        val selectedId = _uiState.value.selectedPackageId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                walletRepository.recharge(selectedId)
                _uiState.update { it.copy(message = "充值成功！资金已即时到账") }
                loadWalletData()
                onSuccess?.invoke()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "充值失败") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
