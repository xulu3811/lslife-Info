package com.lianshan.lslife.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = false,
    val coinBalance: Int = 0,
    val requiredCoins: Int = 0,
    val tradeType: String = "",
    val relatedBizId: String? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    fun initPaywall(requiredCoins: Int, tradeType: String, relatedBizId: String? = null) {
        _uiState.update { 
            it.copy(
                requiredCoins = requiredCoins, 
                tradeType = tradeType, 
                relatedBizId = relatedBizId 
            ) 
        }
        loadBalance()
    }

    private fun loadBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val info = walletRepository.getWalletInfo()
                _uiState.update { it.copy(isLoading = false, coinBalance = info.coinBalance) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "获取余额失败") }
            }
        }
    }

    fun consume() {
        val state = _uiState.value
        if (state.coinBalance < state.requiredCoins) {
            _uiState.update { it.copy(error = "余额不足") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                walletRepository.consumeCoins(state.requiredCoins, state.tradeType, state.relatedBizId)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "支付失败") }
            }
        }
    }
}
