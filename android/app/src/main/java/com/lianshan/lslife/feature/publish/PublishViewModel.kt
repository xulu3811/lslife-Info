package com.lianshan.lslife.feature.publish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.CategoryRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.DynamicField
import com.lianshan.lslife.core.model.Quota
import com.lianshan.lslife.core.network.CreatePostRequest
import com.lianshan.lslife.core.network.AiGenerateDescResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import java.security.MessageDigest

import com.lianshan.lslife.core.model.TradeMode

data class PublishUiState(
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    
    // 分类树与动态 Schema
    val categoryTree: List<CategoryNode> = emptyList(),
    val loadingCategories: Boolean = false,
    val categoryError: String? = null,
    val selectedCategory: CategoryNode? = null,
    val selectedCategoryPath: String = "请选择分类",
    val categoryId: String = "second_hand",
    val requireCategorySelection: Boolean = false,
    val preSelectedLevel1Id: String? = null,

    // Schema-Driven Dynamic Attributes Form State
    val categorySchemas: List<com.lianshan.lslife.core.model.AttributeSchema> = emptyList(),
    val attributesMap: Map<String, Any> = emptyMap(),

    val title: String = "",
    val description: String = "",
    val price: String = "",
    val contactPhone: String = "",
    val tradeMode: TradeMode = TradeMode.INFO_PUBLISH,
    val images: List<String> = emptyList(),
    val location: String = "连山壮族瑶族自治县",
    val quota: Quota? = null,
    val submitting: Boolean = false,
    val aiOptimizing: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
    val editingPostId: String? = null,
    val publishedPostId: String? = null,
    val useUrgentTag: Boolean = false,
    val isKycVerified: Boolean = false,
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
    private val authRepo: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val isEditMode = savedStateHandle.get<String>("postId")?.let { it.isNotBlank() && it != "{postId}" } == true
    private val initialCategoryId = savedStateHandle.get<String>("categoryId")?.takeIf { it.isNotBlank() && it != "{categoryId}" } ?: "second_hand"

    private val _state = MutableStateFlow(PublishUiState(categoryId = initialCategoryId))
    val state: StateFlow<PublishUiState> = _state

    init {
        observeCategories()
        observeKycState()
    }

    private fun observeKycState() {
        viewModelScope.launch {
            authRepo.currentUser.collect { user ->
                _state.update { it.copy(isKycVerified = user?.realNameStatus == "verified") }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree()
        }
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                _state.update { it.copy(categoryTree = tree) }
                if (tree.isNotEmpty() && _state.value.selectedCategory == null) {
                    val initialId = _state.value.categoryId
                    val targetNodeAndPath = findExactNodeAndPath(tree, initialId)
                    if (targetNodeAndPath != null && targetNodeAndPath.first.isLeaf) {
                        onSelectLeafCategory(targetNodeAndPath.first, targetNodeAndPath.second, retainAttributes = true)
                    } else if (!isEditMode) {
                        _state.update { it.copy(
                            requireCategorySelection = true, 
                            preSelectedLevel1Id = targetNodeAndPath?.first?.id
                        ) }
                    }
                }
            }
        }
        viewModelScope.launch {
            categoryRepo.loading.collect { loading ->
                _state.update { it.copy(loadingCategories = loading) }
            }
        }
        viewModelScope.launch {
            categoryRepo.error.collect { err ->
                _state.update { it.copy(categoryError = err) }
            }
        }
    }

    fun retryLoadCategories() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree(forceRefresh = true)
        }
    }

    fun onCategorySelectionShown() = _state.update { it.copy(requireCategorySelection = false) }

    private fun findExactNodeAndPath(nodes: List<CategoryNode>, targetId: String, pathPrefix: String = ""): Pair<CategoryNode, String>? {
        for (node in nodes) {
            val currentPath = if (pathPrefix.isEmpty()) node.name else "$pathPrefix > ${node.name}"
            if (node.id == targetId) return node to currentPath
            if (node.children.isNotEmpty()) {
                val found = findExactNodeAndPath(node.children, targetId, currentPath)
                if (found != null) return found
            }
        }
        return null
    }

    fun loadQuota() {
        viewModelScope.launch {
            repo.quota().onSuccess { q -> _state.update { it.copy(quota = q) } }
        }
    }

    fun onSelectLeafCategory(leafNode: CategoryNode, fullPath: String, retainAttributes: Boolean = false) {
        val mode = leafNode.tradeMode
        val schemas = CategorySchemaRegistry.getCategorySchema(leafNode.id, leafNode.name)
        _state.update {
            it.copy(
                selectedCategory = leafNode,
                selectedCategoryPath = fullPath,
                categoryId = leafNode.id,
                tradeMode = mode,
                categorySchemas = schemas,
                attributesMap = if (retainAttributes) it.attributesMap else emptyMap()
            )
        }
    }

    fun onAttributeChange(key: String, value: Any) {
        _state.update { s ->
            val updated = s.attributesMap.toMutableMap()
            if (value is String && value.isBlank()) {
                updated.remove(key)
            } else if (value is Collection<*> && value.isEmpty()) {
                updated.remove(key)
            } else {
                updated[key] = value
            }
            s.copy(attributesMap = updated)
        }
    }

    fun loadPost(id: String) {
        if (id == "{postId}" || id.isBlank()) return
        viewModelScope.launch {
            repo.post(id).onSuccess { post ->
                val attrs = post.attributes.mapValues { (_, element) ->
                    when {
                        element is kotlinx.serialization.json.JsonPrimitive -> element.content
                        element is kotlinx.serialization.json.JsonArray -> element.map { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString() }
                        else -> element.toString()
                    }
                }
                _state.update {
                    it.copy(
                        editingPostId = post.id,
                        publisherType = post.publisherType,
                        merchantId = post.merchantId,
                        listingType = post.listingType,
                        categoryId = post.category,
                        title = post.title,
                        description = post.description,
                        price = post.price?.toString() ?: "",
                        contactPhone = post.contactPhone ?: "",
                        tradeMode = post.tradeMode,
                        images = post.images,
                        location = post.locationName ?: "连山壮族瑶族自治县",
                        attributesMap = attrs,
                    )
                }
                if (_state.value.categoryTree.isNotEmpty()) {
                    val found = categoryRepo.findLeafCategoryAndPath(_state.value.categoryTree, post.category)
                        ?: categoryRepo.findFirstLeafAndPath(_state.value.categoryTree)
                    if (found != null) {
                        onSelectLeafCategory(found.first, found.second, retainAttributes = true)
                    }
                }
            }
        }
    }

    fun onTitle(v: String) = _state.update { it.copy(title = v.take(30)) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onPrice(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onContactPhone(v: String) = _state.update { it.copy(contactPhone = v.filter { c -> c.isDigit() }) }
    fun onImagesSelected(uris: List<String>) {
        val current = _state.value.images
        _state.update { it.copy(images = current + uris) }
    }
    fun removeImage(uri: String) = _state.update { it.copy(images = it.images - uri) }
    fun onLocation(l: String) = _state.update { it.copy(location = l) }
    fun onPublisherType(type: String, merchantId: String? = null) = _state.update { 
        it.copy(publisherType = type, merchantId = merchantId) 
    }
    fun onListingType(type: String) = _state.update { it.copy(listingType = type) }

    fun generateAiDescription() {
        val s = _state.value
        val hint = if (s.title.isNotBlank()) s.title else s.selectedCategoryPath
        val draft = s.description
        _state.update { it.copy(aiOptimizing = true) }
        viewModelScope.launch {
            val text = "$hint $draft"
            val extractedAttrs = s.attributesMap.toMutableMap()

            // 智能本地抽取逻辑：匹配当前分类 Schema 中的选项与描述文案
            s.categorySchemas.forEach { schema ->
                when (schema.type) {
                    com.lianshan.lslife.core.model.FieldType.SINGLE_CHOICE -> {
                        val match = schema.options.find { opt -> text.contains(opt) }
                        if (match != null && !extractedAttrs.containsKey(schema.key)) {
                            extractedAttrs[schema.key] = match
                        }
                    }
                    com.lianshan.lslife.core.model.FieldType.MULTI_CHOICE -> {
                        val matches = schema.options.filter { opt -> text.contains(opt) }
                        if (matches.isNotEmpty()) {
                            val existing = extractedAttrs[schema.key]
                            val newSet = if (existing is Collection<*>) {
                                (existing.filterNotNull().map { it.toString() } + matches).toSet().toList()
                            } else {
                                matches
                            }
                            extractedAttrs[schema.key] = newSet
                        }
                    }
                    com.lianshan.lslife.core.model.FieldType.NUMBER_INPUT -> {
                        val regex = Regex("(\\d+(\\.\\d+)?)\\s*${schema.unit ?: ""}")
                        val match = regex.find(text)
                        if (match != null && !extractedAttrs.containsKey(schema.key)) {
                            extractedAttrs[schema.key] = match.groupValues[1]
                        }
                    }
                    else -> {}
                }
            }

            // 构造传递给后端的临时 DynamicField 列表
            val tempSchema = s.categorySchemas.map {
                com.lianshan.lslife.core.model.DynamicField(
                    key = it.key,
                    label = it.label,
                    fieldType = it.type.name,
                    options = it.options
                )
            }

            repo.aiGenerateDescription(
                title = hint,
                categoryId = s.categoryId,
                draft = draft,
                schema = tempSchema
            ).onSuccess { res ->
                val newTitle = res.title
                val newDesc = res.description
                
                s.categorySchemas.forEach { field ->
                    val element = res.attributes[field.key]
                    val extractedVal = if (element is kotlinx.serialization.json.JsonPrimitive) element.content else element?.toString() ?: ""
                    if (extractedVal.isNotBlank()) {
                        if (field.type == com.lianshan.lslife.core.model.FieldType.MULTI_CHOICE) {
                             extractedAttrs[field.key] = extractedVal.split(",").map { it.trim() }
                        } else {
                             extractedAttrs[field.key] = extractedVal
                        }
                    }
                }

                _state.update {
                    it.copy(
                        aiOptimizing = false,
                        title = newTitle.ifBlank { s.title },
                        description = newDesc.ifBlank { draft },
                        attributesMap = extractedAttrs,
                        message = "✨ AI 已基于描述智能匹配并自动填入分类属性！"
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        aiOptimizing = false,
                        attributesMap = extractedAttrs,
                        message = "✨ AI 已完成智能属性提取与自动选择！"
                    )
                }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, success = false) }
    fun setMessage(msg: String) = _state.update { it.copy(message = msg) }
    fun setUseUrgentTag(use: Boolean) = _state.update { it.copy(useUrgentTag = use) }

    fun submit() {
        val s = _state.value
        if (s.selectedCategory == null || !s.selectedCategory.isLeaf) {
            _state.update { it.copy(message = "请选择具体的最底层叶子类目后发布") }
            return
        }
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.update { it.copy(message = "请填写标题和描述") }
            return
        }
        if (s.contactPhone.isBlank()) {
            _state.update { it.copy(message = "发布必须填写联系电话") }
            return
        }
        if (s.publisherType == "INDIVIDUAL" && !s.isKycVerified) {
            _state.update { it.copy(message = "您尚未完成实名认证，发布已被拦截") }
            return
        }
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            val md5Hashes = mutableListOf<String>()
            val uploadedUrls = try {
                val httpUrls = mutableListOf<String>()
                val localParts = mutableListOf<okhttp3.MultipartBody.Part>()
                
                // 1. 本地并发无损压缩
                coroutineScope {
                    s.images.mapIndexed { index, uri ->
                        async(Dispatchers.IO) {
                            if (uri.startsWith("http")) {
                                synchronized(httpUrls) { httpUrls.add(uri) }
                            } else {
                                val bytes = ImageCompressor.compress(context, uri)
                                val md5 = MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }
                                synchronized(md5Hashes) { md5Hashes.add(md5) }
                                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                val part = okhttp3.MultipartBody.Part.createFormData("images", "upload_${index}.jpg", reqFile)
                                synchronized(localParts) { localParts.add(part) }
                            }
                        }
                    }.awaitAll()
                }
                
                // 2. 一次性批量合并上传
                if (localParts.isNotEmpty()) {
                    val res = repo.uploadImagesBatch(localParts)
                    if (res.isSuccess) {
                        val batchUrls = res.getOrNull()?.urls ?: throw Exception("批量上传返回空地址")
                        httpUrls.addAll(batchUrls)
                    } else {
                        throw Exception(res.exceptionOrNull()?.message ?: "批量图片上传失败")
                    }
                }
                httpUrls
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, message = "图片上传失败: ${e.message}") }
                return@launch
            }

            val mergedAttributes = kotlinx.serialization.json.buildJsonObject {

                s.attributesMap.forEach { (k, v) ->
                    when (v) {
                        is String -> put(k, kotlinx.serialization.json.JsonPrimitive(v))
                        is Number -> put(k, kotlinx.serialization.json.JsonPrimitive(v))
                        is Boolean -> put(k, kotlinx.serialization.json.JsonPrimitive(v))
                        is Collection<*> -> {
                            val arr = kotlinx.serialization.json.buildJsonArray {
                                v.forEach { item ->
                                    if (item != null) add(kotlinx.serialization.json.JsonPrimitive(item.toString()))
                                }
                            }
                            put(k, arr)
                        }
                        else -> put(k, kotlinx.serialization.json.JsonPrimitive(v.toString()))
                    }
                }
            }

            val req = CreatePostRequest(
                category = s.categoryId,
                title = s.title.ifBlank { "同城优质发布" },
                description = s.description,
                useUrgentTag = s.useUrgentTag,
                price = s.price.toDoubleOrNull(),
                contactPhone = s.contactPhone.takeIf { it.isNotBlank() },
                tradeMode = s.tradeMode.name,
                images = uploadedUrls,
                imageHashes = md5Hashes,
                publisherType = s.publisherType,
                merchantId = s.merchantId,
                listingType = s.listingType,
                attributes = mergedAttributes,
                locationName = s.location,
            )
            
            val result = if (s.editingPostId != null) {
                repo.updatePost(s.editingPostId, req)
            } else {
                repo.createPost(req)
            }
            
            result.onSuccess { post ->
                _state.update { 
                    it.copy(
                        message = if (s.editingPostId != null) "修改成功，已提交审核" else "发布成功", 
                        success = true, 
                        publishedPostId = post.id,
                        submitting = false
                    ) 
                }
                loadQuota()
            }.onFailure { e ->
                _state.update { it.copy(submitting = false, message = e.message ?: "发布失败") }
            }
        }
    }
}
