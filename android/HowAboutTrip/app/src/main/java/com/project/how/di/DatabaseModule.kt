package com.project.how.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.how.roomdb.dao.AlarmDao
import com.project.how.roomdb.dao.RecentAirplaneDao
import com.project.how.roomdb.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `alarm_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `isChecked` INTEGER NOT NULL, `created_at` INTEGER NOT NULL DEFAULT (strftime('%s','now')))")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext.applicationContext,
            AppDatabase::class.java,
            "how_about_trip_database"
        )
            .addMigrations(MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideRecentAirplaneDao(database: AppDatabase): RecentAirplaneDao {
        return database.recentAirplaneDao()
    }

    @Provides
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }
}
