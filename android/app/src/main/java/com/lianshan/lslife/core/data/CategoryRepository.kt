package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.CategorySchemaResponse
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局分类数据仓储 (Single Source of Truth - SSOT)
 * 统一管理分类树的加载、缓存与动态 Schema 检索。
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val api: ApiService
) {
    private val _categoryTree = MutableStateFlow<List<CategoryNode>>(emptyList())
    val categoryTree: StateFlow<List<CategoryNode>> = _categoryTree.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val schemaCache = mutableMapOf<String, CategorySchemaResponse>()

    suspend fun getCategoryTree(forceRefresh: Boolean = false): Result<List<CategoryNode>> {
        if (!forceRefresh && _categoryTree.value.isNotEmpty()) {
            return Result.success(_categoryTree.value)
        }
        _loading.value = true
        _error.value = null
        
        val res = safeCall { api.getCategoryTree() }
        _loading.value = false
        
        res.onSuccess { tree ->
            _categoryTree.value = tree
            _error.value = null
        }.onFailure { e ->
            if (_categoryTree.value.isEmpty()) {
                _error.value = e.message ?: "获取分类数据失败"
            }
        }
        return res
    }

    suspend fun getCategorySchema(id: String): Result<CategorySchemaResponse> {
        schemaCache[id]?.let { return Result.success(it) }
        val res = safeCall { api.getCategorySchema(id) }
        res.onSuccess { schemaCache[id] = it }
        return res
    }

    /** 寻找特定的分类节点，如果是叶子节点直接返回；如果不是，则返回其下的第一个有效叶子节点及其完整层级路径 */
    fun findLeafCategoryAndPath(tree: List<CategoryNode>, targetId: String): Pair<CategoryNode, String>? {
        fun findNodeAndPath(nodes: List<CategoryNode>, pathPrefix: String): Pair<CategoryNode, String>? {
            for (node in nodes) {
                val currentPath = if (pathPrefix.isEmpty()) node.name else "$pathPrefix > ${node.name}"
                if (node.id == targetId) return node to currentPath
                if (node.children.isNotEmpty()) {
                    val found = findNodeAndPath(node.children, currentPath)
                    if (found != null) return found
                }
            }
            return null
        }

        val target = findNodeAndPath(tree, "") ?: return null
        val (node, path) = target

        if (node.isLeaf) return node to path

        fun findFirstLeaf(n: CategoryNode, p: String): Pair<CategoryNode, String>? {
            if (n.isLeaf) return n to p
            for (child in n.children) {
                val found = findFirstLeaf(child, "$p > ${child.name}")
                if (found != null) return found
            }
            return null
        }

        return findFirstLeaf(node, path)
    }

    /** 寻找树中的第一个有效叶子节点及其完整层级路径作为兜底 */
    fun findFirstLeafAndPath(tree: List<CategoryNode>): Pair<CategoryNode, String>? {
        fun search(nodes: List<CategoryNode>, pathPrefix: String): Pair<CategoryNode, String>? {
            for (node in nodes) {
                val currentPath = if (pathPrefix.isEmpty()) node.name else "$pathPrefix > ${node.name}"
                if (node.isLeaf) {
                    return Pair(node, currentPath)
                }
                if (node.children.isNotEmpty()) {
                    val found = search(node.children, currentPath)
                    if (found != null) return found
                }
            }
            return null
        }
        return search(tree, "")
    }
}
