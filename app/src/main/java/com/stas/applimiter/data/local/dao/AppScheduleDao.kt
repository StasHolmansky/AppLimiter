package com.stas.applimiter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stas.applimiter.data.local.entity.AppScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppScheduleDao {

    @Query("SELECT * FROM app_schedules")
    fun getAll(): Flow<List<AppScheduleEntity>>

    @Query("SELECT * FROM app_schedules")
    suspend fun getAllOnce(): List<AppScheduleEntity>

    @Query("SELECT * FROM app_schedules WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): AppScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: AppScheduleEntity)

    @Query("DELETE FROM app_schedules WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
