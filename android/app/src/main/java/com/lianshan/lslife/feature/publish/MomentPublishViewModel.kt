package com.lianshan.lslife.feature.publish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.network.CreatePostRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MomentPublishViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images: StateFlow<List<String>> = _images.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _topic = MutableStateFlow<String?>(null)
    val topic: StateFlow<String?> = _topic.asStateFlow()

    private val _linkedCommerceId = MutableStateFlow<String?>(null)
    val linkedCommerceId: StateFlow<String?> = _linkedCommerceId.asStateFlow()

    private val _momentType = MutableStateFlow<String?>(null)
    val momentType: StateFlow<String?> = _momentType.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _budget = MutableStateFlow("")
    val budget: StateFlow<String> = _budget.asStateFlow()

    private val _isUrgent = MutableStateFlow(false)
    val isUrgent: StateFlow<Boolean> = _isUrgent.asStateFlow()

    private val _selectedTheme = MutableStateFlow<String?>(null)
    val selectedTheme: StateFlow<String?> = _selectedTheme.asStateFlow()

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun setMomentType(type: String?) {
        _momentType.value = type
    }

    fun setRating(value: Int) {
        _rating.value = value
    }

    fun setBudget(value: String) {
        _budget.value = value
    }

    fun setUrgent(value: Boolean) {
        _isUrgent.value = value
    }

    fun setTheme(theme: String?) {
        _selectedTheme.value = theme
    }

    fun setTopic(newTopic: String?) {
        _topic.value = newTopic
        if (newTopic != null && !_content.value.contains(newTopic)) {
            _content.value = "$newTopic ${_content.value}"
        }
    }

    fun setLinkedCommerceId(id: String?) {
        _linkedCommerceId.value = id
    }

    fun addImages(uris: List<String>) {
        _images.value = (_images.value + uris).take(9)
    }

    fun removeImage(index: Int) {
        val current = _images.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _images.value = current
        }
    }

    suspend fun fetchMyCommerceItems(): List<com.lianshan.lslife.core.model.Post> {
        val result = repository.posts(mine = true, postType = "COMMERCE", pageSize = 50)
        return result.getOrNull()?.list ?: emptyList()
    }

    fun publish(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_content.value.isBlank()) {
            onError("请输入新鲜事内容")
            return
        }
        
        viewModelScope.launch {
            _isPublishing.value = true
            
            val attributesMap = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
            if (_rating.value > 0) {
                attributesMap["rating"] = kotlinx.serialization.json.JsonPrimitive(_rating.value)
            }
            if (_budget.value.isNotBlank()) {
                attributesMap["budget"] = kotlinx.serialization.json.JsonPrimitive(_budget.value)
            }
            if (_isUrgent.value) {
                attributesMap["isUrgent"] = kotlinx.serialization.json.JsonPrimitive(true)
            }
            if (_selectedTheme.value != null) {
                attributesMap["theme"] = kotlinx.serialization.json.JsonPrimitive(_selectedTheme.value)
            }
            if (_momentType.value != null) {
                attributesMap["momentType"] = kotlinx.serialization.json.JsonPrimitive(_momentType.value)
            }

            val request = CreatePostRequest(
                category = "dynamic_post",
                title = _title.value,
                description = _content.value,
                images = _images.value,
                postType = "MOMENT",
                listingType = "INFO",
                tradeMode = "INFO",
                price = null,
                originalPrice = null,
                merchantId = null,
                topic = _topic.value,
                linkedCommerceId = _linkedCommerceId.value,
                attributes = kotlinx.serialization.json.JsonObject(attributesMap)
            )
            
            val result = repository.createPost(request)
            _isPublishing.value = false
            
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "发布失败")
            }
        }
    }
}
