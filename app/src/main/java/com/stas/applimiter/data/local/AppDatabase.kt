package com.stas.applimiter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stas.applimiter.data.local.dao.AppLimitDao
import com.stas.applimiter.data.local.dao.AppScheduleDao
import com.stas.applimiter.data.local.dao.LimitExtensionDao
import com.stas.applimiter.data.local.entity.AppLimitEntity
import com.stas.applimiter.data.local.entity.AppScheduleEntity
import com.stas.applimiter.data.local.entity.LimitExtensionEntity


@Database(
    entities = [
        AppLimitEntity::class,
        LimitExtensionEntity::class,
        AppScheduleEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appLimitDao(): AppLimitDao

    abstract fun limitExtensionDao(): LimitExtensionDao

    abstract fun appScheduleDao(): AppScheduleDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `limit_extensions` (
                        `packageName` TEXT NOT NULL,
                        `dayKey` INTEGER NOT NULL,
                        `bonusMinutes` INTEGER NOT NULL,
                        PRIMARY KEY(`packageName`, `dayKey`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_schedules` (
                        `packageName` TEXT NOT NULL,
                        `appName` TEXT NOT NULL,
                        `allowFromMinutes` INTEGER NOT NULL,
                        `allowUntilMinutes` INTEGER NOT NULL,
                        PRIMARY KEY(`packageName`)
                    )
                    """.trimIndent()
                )
            }
        }
    }

}