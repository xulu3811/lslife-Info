package com.lianshan.lslife.feature.profile

import android.content.Context
import android.net.Uri
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
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

    var currentStep = mutableStateOf(1)
    
    var certType = mutableStateOf("ENTERPRISE")
    var storeName = mutableStateOf("")
    var categoryId = mutableStateOf("")
    var contactName = mutableStateOf("")
    var contactPhone = mutableStateOf("")
    
    var businessLicenseUrl = mutableStateOf("")
    var storePhotos = mutableStateOf<List<String>>(emptyList())
    
    // OCR fields
    var legalPerson = mutableStateOf("")
    var creditCode = mutableStateOf("")

    init {
        loadDraft()
    }

    private fun loadDraft() {
        viewModelScope.launch {
            repository.getMerchantCertifyStatus().onSuccess { cert ->
                if (cert != null && cert.status == "DRAFT") {
                    certType.value = cert.certType
                    storeName.value = cert.storeName
                    categoryId.value = cert.categoryId
                    contactName.value = cert.contactName
                    contactPhone.value = cert.contactPhone
                    businessLicenseUrl.value = cert.businessLicenseUrl ?: ""
                    storePhotos.value = cert.storePhotos
                }
            }
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            val req = MerchantCertifyRequest(
                certType = certType.value,
                storeName = storeName.value,
                categoryId = categoryId.value,
                contactName = contactName.value,
                contactPhone = contactPhone.value,
                businessLicenseUrl = businessLicenseUrl.value.ifBlank { null },
                storePhotos = storePhotos.value,
                isDraft = true
            )
            repository.submitMerchantCertification(req)
        }
    }

    fun nextStep() {
        if (currentStep.value < 3) {
            currentStep.value += 1
            saveDraft()
        }
    }

    fun prevStep() {
        if (currentStep.value > 1) {
            currentStep.value -= 1
        }
    }
    
    private fun createMultipart(uri: Uri, context: Context, filename: String): MultipartBody.Part? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, filename)
        tempFile.outputStream().use { out -> inputStream.use { it.copyTo(out) } }
        
        val compressedBytes = com.lianshan.lslife.feature.publish.ImageCompressor.compress(context, tempFile.absolutePath, 2 * 1024 * 1024, 1920)
        
        val reqFile = okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), compressedBytes)
        return MultipartBody.Part.createFormData("images", filename, reqFile)
    }
    
    fun uploadLicenseAndPerformOcr(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = CertifyState.Loading
            try {
                val part = createMultipart(uri, context, "license.jpg") ?: return@launch
                val uploadRes = repository.uploadImagesBatch(listOf(part))
                if (uploadRes.isSuccess && uploadRes.getOrNull()?.urls?.isNotEmpty() == true) {
                    val url = uploadRes.getOrNull()!!.urls[0]
                    businessLicenseUrl.value = url
                    
                    // OCR
                    repository.performBusinessOcr(url).onSuccess {
                        legalPerson.value = it.legalPerson
                        creditCode.value = it.creditCode
                        _uiState.value = CertifyState.Idle
                    }.onFailure {
                        _uiState.value = CertifyState.Error("OCR识别失败")
                    }
                } else {
                    _uiState.value = CertifyState.Error("图片上传失败")
                }
            } catch (e: Exception) {
                _uiState.value = CertifyState.Error("识别异常")
            }
        }
    }
    
    fun uploadStorePhotos(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            _uiState.value = CertifyState.Loading
            try {
                val parts = uris.mapIndexedNotNull { index, uri ->
                    createMultipart(uri, context, "store_${index}.jpg")
                }
                val uploadRes = repository.uploadImagesBatch(parts)
                if (uploadRes.isSuccess) {
                    storePhotos.value = uploadRes.getOrNull()?.urls ?: emptyList()
                    _uiState.value = CertifyState.Idle
                } else {
                    _uiState.value = CertifyState.Error("门店照片上传失败")
                }
            } catch (e: Exception) {
                _uiState.value = CertifyState.Error("上传异常")
            }
        }
    }

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
                    storePhotos = storePhotos.value,
                    isDraft = false
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
