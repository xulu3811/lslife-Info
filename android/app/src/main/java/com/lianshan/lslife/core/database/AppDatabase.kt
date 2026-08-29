package com.qingyuan.lslife.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MerchantEntity::class, 
        ChatSessionEntity::class,
        LocalMessageEntity::class,
        LocalConversationEntity::class
    ], 
    version = 3, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun merchantDao(): MerchantDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun imDao(): ImDao
}
