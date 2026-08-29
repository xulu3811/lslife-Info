package com.qingyuan.lslife.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.model.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import com.qingyuan.lslife.core.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val merchant: Merchant? = null,
    val message: String? = null,
)

@HiltViewModel
class MerchantDetailViewModel @Inject constructor(
    private val repo: LsRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    val currentUserId: String? = authRepo.cachedMe()?.id
    private val _state = MutableStateFlow(MerchantUiState())
    val state: StateFlow<MerchantUiState> = _state

    fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repo.merchant(id)
                .onSuccess { m -> _state.update { it.copy(loading = false, merchant = m) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
