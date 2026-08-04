package com.lianshan.lslife.feature.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.network.MerchantCertifyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CertifyState {
    object Idle : CertifyState()
    object Loading : CertifyState()
    object Success : CertifyState()
    data class Error(val message: String) : CertifyState()
}

@HiltViewModel
class MerchantCertifyViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CertifyState>(CertifyState.Idle)
    val uiState: StateFlow<CertifyState> = _uiState.asStateFlow()

    var certType = mutableStateOf("ENTERPRISE")
    var storeName = mutableStateOf("")
    var categoryId = mutableStateOf("")
    var contactName = mutableStateOf("")
    var contactPhone = mutableStateOf("")
    var businessLicenseUrl = mutableStateOf("")
    var storePhotos = mutableStateOf<List<String>>(emptyList())

    fun submitCertification() {
        if (storeName.value.isBlank() || contactName.value.isBlank() || contactPhone.value.isBlank()) {
            _uiState.value = CertifyState.Error("请填写全部必填项")
            return
        }

        viewModelScope.launch {
            _uiState.value = CertifyState.Loading
            try {
                val req = MerchantCertifyRequest(
                    certType = certType.value,
                    storeName = storeName.value,
                    categoryId = categoryId.value,
                    contactName = contactName.value,
                    contactPhone = contactPhone.value,
                    businessLicenseUrl = businessLicenseUrl.value.ifBlank { null },
                    storePhotos = storePhotos.value
                )
                val result = repository.submitMerchantCertification(req)
                result.fold(
                    onSuccess = {
                        _uiState.value = CertifyState.Success
                    },
                    onFailure = {
                        _uiState.value = CertifyState.Error(it.message ?: "提交失败，请稍后重试")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = CertifyState.Error(e.message ?: "网络异常")
            }
        }
    }
}
