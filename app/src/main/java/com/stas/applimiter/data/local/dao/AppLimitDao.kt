package com.stas.applimiter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stas.applimiter.data.local.entity.AppLimitEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AppLimitDao {


    @Query(
        "SELECT * FROM app_limits"
    )
    fun getAll():
            Flow<List<AppLimitEntity>>

    @Query(
        "SELECT * FROM app_limits"
    )
    suspend fun getAllOnce(): List<AppLimitEntity>

    @Query(
        "SELECT * FROM app_limits WHERE packageName = :packageName LIMIT 1"
    )
    suspend fun getByPackageName(
        packageName: String
    ): AppLimitEntity?


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(
        limit: AppLimitEntity
    )


    @Query(
        "DELETE FROM app_limits WHERE packageName = :packageName"
    )
    suspend fun delete(
        packageName: String
    )

}