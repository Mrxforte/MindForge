package com.example.core.data.repository

import com.example.core.data.database.AppDatabase
import com.example.core.data.database.entity.GameProgressEntity
import com.example.core.data.database.entity.GameSessionEntity
import com.example.core.data.database.entity.UserProfileEntity
import com.example.core.gameengine.CatalogProvider
import com.example.core.model.GameCategory
import com.example.core.model.GameDefinition
import com.example.core.model.GameResult
import com.example.core.model.LevelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LevelDisplayItem(
  val levelNumber: Int,
  val isUnlocked: Boolean,
  val isCompleted: Boolean,
  val stars: Int,
  val highScore: Int,
  val config: LevelConfig
)

data class CognitiveStats(
  val totalSessions: Int,
  val totalXp: Int,
  val currentStreak: Int,
  val averageAccuracy: Int,
  val levelsCompletedCount: Int,
  val categoryScores: Map<GameCategory, Int>
)

class GameRepository(private val database: AppDatabase) {

  private val progressDao = database.gameProgressDao()
  private val sessionDao = database.gameSessionDao()
  private val profileDao = database.userProfileDao()
  private val workoutDao = database.dailyWorkoutDao()

  fun getAllGames(): List<GameDefinition> = CatalogProvider.games

  fun getGame(gameId: String): GameDefinition? = CatalogProvider.getGameById(gameId)

  fun getLevelsForGame(gameId: String): Flow<List<LevelDisplayItem>> {
    val game = getGame(gameId) ?: CatalogProvider.games.first()
    return progressDao.getProgressForGame(gameId).map { savedProgressList ->
      val progressMap = savedProgressList.associateBy { it.levelNumber }
      (1..game.totalLevels).map { lvl ->
        val entity = progressMap[lvl]
        // Level 1 is always unlocked by default; Level N is unlocked if level N-1 is completed or explicitly unlocked
        val isFirstLevel = lvl == 1
        val prevCompleted = progressMap[lvl - 1]?.isCompleted == true
        val isUnlocked = entity?.isUnlocked == true || isFirstLevel || prevCompleted

        LevelDisplayItem(
          levelNumber = lvl,
          isUnlocked = isUnlocked,
          isCompleted = entity?.isCompleted == true,
          stars = entity?.stars ?: 0,
          highScore = entity?.highScore ?: 0,
          config = CatalogProvider.getLevelConfig(gameId, lvl)
        )
      }
    }
  }

  suspend fun recordGameCompletion(result: GameResult): Boolean = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val existing = progressDao.getProgress(result.gameId, result.levelNumber)
    val isNewHigh = result.score > (existing?.highScore ?: 0)

    val updatedProgress = GameProgressEntity(
      gameId = result.gameId,
      levelNumber = result.levelNumber,
      isUnlocked = true,
      isCompleted = result.isCompleted || (existing?.isCompleted == true),
      stars = maxOf(result.stars, existing?.stars ?: 0),
      highScore = maxOf(result.score, existing?.highScore ?: 0),
      bestTimeMs = if (existing == null || existing.bestTimeMs == 0L || result.durationMs < existing.bestTimeMs) {
        result.durationMs
      } else {
        existing.bestTimeMs
      },
      lastPlayedTimestamp = now
    )
    progressDao.insertOrUpdate(updatedProgress)

    // Unlock next level if completed
    val game = getGame(result.gameId)
    if (result.isCompleted && game != null && result.levelNumber < game.totalLevels) {
      val nextLvl = result.levelNumber + 1
      val nextExisting = progressDao.getProgress(result.gameId, nextLvl)
      if (nextExisting == null || !nextExisting.isUnlocked) {
        progressDao.insertOrUpdate(
          nextExisting?.copy(isUnlocked = true) ?: GameProgressEntity(
            gameId = result.gameId,
            levelNumber = nextLvl,
            isUnlocked = true
          )
        )
      }
    }

    // Save session record
    sessionDao.insertSession(
      GameSessionEntity(
        gameId = result.gameId,
        levelNumber = result.levelNumber,
        score = result.score,
        accuracy = result.accuracy,
        durationMs = result.durationMs,
        xpEarned = result.xpEarned,
        completed = result.isCompleted,
        timestamp = now
      )
    )

    // Update user profile XP and streak
    val profile = profileDao.getProfileSync() ?: UserProfileEntity()
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
    val isNewDay = profile.lastActiveDate.isNotEmpty() && profile.lastActiveDate != todayDate
    val updatedStreak = if (isNewDay) profile.currentStreak + 1 else profile.currentStreak.coerceAtLeast(1)

    profileDao.saveProfile(
      profile.copy(
        totalXp = profile.totalXp + result.xpEarned,
        currentStreak = updatedStreak,
        bestStreak = maxOf(profile.bestStreak, updatedStreak),
        lastActiveDate = todayDate
      )
    )

    isNewHigh
  }

  fun getUserProfile(): Flow<UserProfileEntity> = profileDao.getProfile().map { it ?: UserProfileEntity() }

  suspend fun updateUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
    profileDao.saveProfile(profile)
  }

  fun getRecentSessions(limit: Int = 20): Flow<List<GameSessionEntity>> = sessionDao.getRecentSessions(limit)

  suspend fun calculateStats(): CognitiveStats = withContext(Dispatchers.IO) {
    val totalSessions = sessionDao.getTotalSessionsCount()
    val avgAcc = (sessionDao.getAverageAccuracy() ?: 85.0).toInt()
    val allProgress = progressDao.getAllProgress().first()
    val completedCount = allProgress.count { it.isCompleted }
    val profile = profileDao.getProfileSync() ?: UserProfileEntity()

    // Calculate category breakdown based on completed levels and scores
    val catMap = mutableMapOf<GameCategory, Int>()
    for (category in GameCategory.entries) {
      val catGames = CatalogProvider.games.filter { it.category == category }.map { it.id }.toSet()
      val catCompleted = allProgress.filter { it.gameId in catGames && it.isCompleted }.size
      val catBase = (50 + catCompleted * 4).coerceIn(40, 100)
      catMap[category] = catBase
    }

    CognitiveStats(
      totalSessions = totalSessions,
      totalXp = profile.totalXp,
      currentStreak = profile.currentStreak,
      averageAccuracy = avgAcc,
      levelsCompletedCount = completedCount,
      categoryScores = catMap
    )
  }

  suspend fun resetAllData() = withContext(Dispatchers.IO) {
    progressDao.deleteAll()
    sessionDao.deleteAll()
    workoutDao.deleteAll()
    profileDao.saveProfile(UserProfileEntity())
  }
}
