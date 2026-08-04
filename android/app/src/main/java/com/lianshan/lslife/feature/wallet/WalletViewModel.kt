package com.lianshan.lslife.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.model.RechargePackage
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
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWalletData()
    }

    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val walletInfo = walletRepository.getWalletInfo()
                val pkgList = walletRepository.getPackages()

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        coinBalance = walletInfo.coinBalance,
                        packages = pkgList,
                        selectedPackageId = pkgList.firstOrNull()?.id // 默认选中首个套餐
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
            }
        }
    }

    fun selectPackage(packageId: Int) {
        _uiState.update { it.copy(selectedPackageId = packageId) }
    }

    fun recharge() {
        val selectedId = _uiState.value.selectedPackageId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                walletRepository.recharge(selectedId)
                // 充值成功后重新加载钱包数据
                loadWalletData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "充值失败") }
            }
        }
    }
}
