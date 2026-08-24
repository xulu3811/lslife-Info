package com.lianshan.lslife.feature.publish

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.network.CreatePostRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

sealed class PublishMomentState {
    object Idle : PublishMomentState()
    object Publishing : PublishMomentState()
    object Success : PublishMomentState()
    data class Error(val message: String) : PublishMomentState()
}

@HiltViewModel
class PublishMomentViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PublishMomentState>(PublishMomentState.Idle)
    val uiState: StateFlow<PublishMomentState> = _uiState.asStateFlow()

    fun publishMoment(context: Context, text: String, imageUris: List<Uri>) {
        if (text.isBlank() && imageUris.isEmpty()) {
            _uiState.value = PublishMomentState.Error("动态内容不能为空")
            return
        }

        _uiState.value = PublishMomentState.Publishing

        viewModelScope.launch {
            try {
                val uploadedUrls = mutableListOf<String>()
                if (imageUris.isNotEmpty()) {
                    val parts = imageUris.mapNotNull { uri -> uriToMultipartBodyPart(context, uri) }
                    if (parts.isNotEmpty()) {
                        val uploadRes = repository.uploadImagesBatch(parts)
                        if (uploadRes.isSuccess) {
                            val resultData = uploadRes.getOrNull()
                            if (resultData != null) {
                                uploadedUrls.addAll(resultData.urls)
                            }
                        } else {
                            _uiState.value = PublishMomentState.Error("图片上传失败")
                            return@launch
                        }
                    }
                }

                val attributesJson = kotlinx.serialization.json.buildJsonObject {
                    put("text", kotlinx.serialization.json.JsonPrimitive(text))
                }

                val request = CreatePostRequest(
                    category = "sys_dynamic",
                    title = "", // No title for moments
                    description = text,
                    price = null,
                    images = uploadedUrls,
                    attributes = attributesJson,
                    postType = "MOMENT"
                )

                val result = repository.createPost(request)
                if (result.isSuccess) {
                    _uiState.value = PublishMomentState.Success
                } else {
                    _uiState.value = PublishMomentState.Error(result.exceptionOrNull()?.message ?: "发布失败")
                }
            } catch (e: Exception) {
                _uiState.value = PublishMomentState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun resetState() {
        _uiState.value = PublishMomentState.Idle
    }

    private fun uriToMultipartBodyPart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val file = getFileFromUri(context, uri) ?: return null
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile) // uploadImagesBatch expects 'images' as part name
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        var file: File? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return null
            
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream) ?: return null
            
            file = File(context.cacheDir, "moment_image_${System.currentTimeMillis()}.webp")
            outputStream = FileOutputStream(file)
            
            val compressFormat = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                android.graphics.Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                android.graphics.Bitmap.CompressFormat.WEBP
            }
            
            bitmap.compress(compressFormat, 80, outputStream)
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
        return file
    }
}
