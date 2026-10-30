package com.example.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_progress", primaryKeys = ["gameId", "levelNumber"])
data class GameProgressEntity(
  val gameId: String,
  val levelNumber: Int,
  val isUnlocked: Boolean = false,
  val isCompleted: Boolean = false,
  val stars: Int = 0,
  val highScore: Int = 0,
  val bestTimeMs: Long = 0L,
  val lastPlayedTimestamp: Long = 0L
)

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val gameId: String,
  val levelNumber: Int,
  val score: Int,
  val accuracy: Int,
  val durationMs: Long,
  val xpEarned: Int,
  val completed: Boolean,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey
  val id: Int = 1,
  val totalXp: Int = 0,
  val currentStreak: Int = 1,
  val bestStreak: Int = 1,
  val lastActiveDate: String = "",
  val onboardingCompleted: Boolean = false,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val themeMode: String = "SYSTEM",
  val language: String = "en"
)

@Entity(tableName = "daily_workouts")
data class DailyWorkoutEntity(
  @PrimaryKey
  val date: String, // YYYY-MM-DD
  val isCompleted: Boolean = false,
  val completedGamesCount: Int = 0,
  val totalGamesCount: Int = 4,
  val totalScore: Int = 0,
  val timestamp: Long = System.currentTimeMillis()
)
