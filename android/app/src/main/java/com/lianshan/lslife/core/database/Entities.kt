package com.lianshan.lslife.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 商家离线缓存, 用于弱网/断网首页展示 (offline-first)。 */
@Entity(tableName = "merchant_cache")
data class MerchantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val distance: Double,
    val sales: Int,
    val avgPrice: Double,
    val tags: String,
    val deliveryFee: Double,
    val deliveryTime: Int,
    val logo: String,
    val category: String?,
    val cachedAt: Long,
)
