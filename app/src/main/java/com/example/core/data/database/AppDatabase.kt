package com.example.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.data.database.dao.DailyWorkoutDao
import com.example.core.data.database.dao.GameProgressDao
import com.example.core.data.database.dao.GameSessionDao
import com.example.core.data.database.dao.UserProfileDao
import com.example.core.data.database.entity.DailyWorkoutEntity
import com.example.core.data.database.entity.GameProgressEntity
import com.example.core.data.database.entity.GameSessionEntity
import com.example.core.data.database.entity.UserProfileEntity

@Database(
  entities = [
    GameProgressEntity::class,
    GameSessionEntity::class,
    UserProfileEntity::class,
    DailyWorkoutEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun gameProgressDao(): GameProgressDao
  abstract fun gameSessionDao(): GameSessionDao
  abstract fun userProfileDao(): UserProfileDao
  abstract fun dailyWorkoutDao(): DailyWorkoutDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "mindforge_database.db"
        )
          .fallbackToDestructiveMigration(true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
