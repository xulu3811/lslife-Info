package com.lianshan.lslife.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverSection(
    val categoryId: String,
    val categoryName: String,
    val posts: List<Post>
)
