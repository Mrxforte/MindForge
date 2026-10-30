package com.example.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.data.database.entity.DailyWorkoutEntity
import com.example.core.data.database.entity.GameProgressEntity
import com.example.core.data.database.entity.GameSessionEntity
import com.example.core.data.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProgressDao {
  @Query("SELECT * FROM game_progress WHERE gameId = :gameId ORDER BY levelNumber ASC")
  fun getProgressForGame(gameId: String): Flow<List<GameProgressEntity>>

  @Query("SELECT * FROM game_progress WHERE gameId = :gameId AND levelNumber = :levelNumber LIMIT 1")
  suspend fun getProgress(gameId: String, levelNumber: Int): GameProgressEntity?

  @Query("SELECT * FROM game_progress")
  fun getAllProgress(): Flow<List<GameProgressEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(progress: GameProgressEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(list: List<GameProgressEntity>)

  @Query("DELETE FROM game_progress")
  suspend fun deleteAll()
}

@Dao
interface GameSessionDao {
  @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentSessions(limit: Int = 50): Flow<List<GameSessionEntity>>

  @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY timestamp DESC")
  fun getSessionsForGame(gameId: String): Flow<List<GameSessionEntity>>

  @Query("SELECT * FROM game_sessions")
  fun getAllSessions(): Flow<List<GameSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: GameSessionEntity): Long

  @Query("SELECT COUNT(*) FROM game_sessions")
  suspend fun getTotalSessionsCount(): Int

  @Query("SELECT AVG(accuracy) FROM game_sessions")
  suspend fun getAverageAccuracy(): Double?

  @Query("DELETE FROM game_sessions")
  suspend fun deleteAll()
}

@Dao
interface UserProfileDao {
  @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
  fun getProfile(): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
  suspend fun getProfileSync(): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveProfile(profile: UserProfileEntity)

  @Update
  suspend fun updateProfile(profile: UserProfileEntity)

  @Query("DELETE FROM user_profile")
  suspend fun deleteAll()
}

@Dao
interface DailyWorkoutDao {
  @Query("SELECT * FROM daily_workouts WHERE date = :date LIMIT 1")
  fun getDailyWorkout(date: String): Flow<DailyWorkoutEntity?>

  @Query("SELECT * FROM daily_workouts WHERE date = :date LIMIT 1")
  suspend fun getDailyWorkoutSync(date: String): DailyWorkoutEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(workout: DailyWorkoutEntity)

  @Query("SELECT * FROM daily_workouts ORDER BY date DESC LIMIT 30")
  fun getPastMonthWorkouts(): Flow<List<DailyWorkoutEntity>>

  @Query("DELETE FROM daily_workouts")
  suspend fun deleteAll()
}
