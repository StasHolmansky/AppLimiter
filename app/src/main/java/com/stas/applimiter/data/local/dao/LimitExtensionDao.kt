package com.stas.applimiter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stas.applimiter.data.local.entity.LimitExtensionEntity

@Dao
interface LimitExtensionDao {

    @Query("SELECT * FROM limit_extensions WHERE dayKey = :dayKey")
    suspend fun getAllForDay(dayKey: Int): List<LimitExtensionEntity>

    @Query(
        """
        SELECT * FROM limit_extensions
        WHERE packageName = :packageName AND dayKey = :dayKey
        LIMIT 1
        """
    )
    suspend fun getForPackageAndDay(
        packageName: String,
        dayKey: Int
    ): LimitExtensionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(extension: LimitExtensionEntity): Long

    @Query("DELETE FROM limit_extensions WHERE dayKey != :dayKey")
    suspend fun deleteExceptDay(dayKey: Int)
}
