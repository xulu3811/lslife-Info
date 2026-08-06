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
            // --- Fallback/Override to guarantee correct TradeMode even if backend is outdated ---
            val commerceIds = setOf("cat_idle", "cat_veggies", "cat_service", "cat_maintenance", "cat_dining")
            val infoIds = setOf("cat_house", "cat_house_sale", "cat_house_rent", "cat_job", "cat_part_time", "cat_car_rental", "cat_education")
            
            fun fixTradeMode(nodes: List<CategoryNode>, parentId: String?): List<CategoryNode> {
                val newNodes = mutableListOf<CategoryNode>()
                for (node in nodes) {
                    val topId = parentId ?: node.id
                    if (parentId == null && node.id == "cat_house") {
                        val saleChildren = node.children.filter { !it.id.contains("rent", ignoreCase = true) && !it.name.contains("租") }
                        val rentChildren = node.children.filter { it.id.contains("rent", ignoreCase = true) || it.name.contains("租") }
                        newNodes.add(
                            CategoryNode(
                                id = "cat_house_sale",
                                name = "二手房源",
                                icon = "home",
                                iconUrl = "/assets/icons/3d_flat_house_sale.png?v=8",
                                sortOrder = 2,
                                isLeaf = false,
                                isActive = true,
                                children = fixTradeMode(saleChildren, "cat_house_sale"),
                                tradeMode = com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH
                            )
                        )
                        newNodes.add(
                            CategoryNode(
                                id = "cat_house_rent",
                                name = "租房",
                                icon = "home",
                                iconUrl = "/assets/icons/3d_flat_house_rent.png?v=8",
                                sortOrder = 3,
                                isLeaf = false,
                                isActive = true,
                                children = fixTradeMode(rentChildren, "cat_house_rent"),
                                tradeMode = com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH
                            )
                        )
                    } else if (parentId == null && node.id == "cat_job") {
                        val partTimeNode = nodes.find { it.id == "cat_part_time" }
                        val allJobChildren = node.children.toMutableList()
                        if (partTimeNode != null) {
                            allJobChildren.addAll(partTimeNode.children)
                        }
                        newNodes.add(
                            node.copy(
                                name = "求职招聘",
                                iconUrl = "/assets/icons/3d_flat_jobs.png",
                                children = fixTradeMode(allJobChildren, "cat_job"),
                                tradeMode = com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH
                            )
                        )
                    } else if (parentId == null && node.id == "cat_part_time") {
                        // Skip, it is merged into cat_job
                    } else {
                        val fixedMode = when {
                            commerceIds.contains(topId) -> com.lianshan.lslife.core.model.TradeMode.O2O_STORE
                            infoIds.contains(topId) -> com.lianshan.lslife.core.model.TradeMode.INFO_PUBLISH
                            else -> node.tradeMode
                        }
                        val finalMode = if (topId == "cat_idle") com.lianshan.lslife.core.model.TradeMode.C2C_IDLE else fixedMode
                        newNodes.add(node.copy(tradeMode = finalMode, children = fixTradeMode(node.children, topId)))
                    }
                }
                return newNodes
            }
            val fixedTree = fixTradeMode(tree, null)
            // ----------------------------------------------------------------------------------
            _categoryTree.value = fixedTree
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
