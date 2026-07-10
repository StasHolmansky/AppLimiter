package com.stas.applimiter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stas.applimiter.data.local.dao.AppLimitDao
import com.stas.applimiter.data.local.dao.LimitExtensionDao
import com.stas.applimiter.data.local.entity.AppLimitEntity
import com.stas.applimiter.data.local.entity.LimitExtensionEntity


@Database(
    entities = [
        AppLimitEntity::class,
        LimitExtensionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appLimitDao(): AppLimitDao

    abstract fun limitExtensionDao(): LimitExtensionDao

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
    }

}