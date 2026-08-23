package com.lianshan.lslife.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchant_cache ORDER BY sales DESC")
    suspend fun getAll(): List<MerchantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MerchantEntity>)

    @Query("DELETE FROM merchant_cache")
    suspend fun clear()
}
