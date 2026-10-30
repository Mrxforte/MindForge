package com.example.core.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AttentionColor
import com.example.ui.theme.LogicColor
import com.example.ui.theme.MathColor
import com.example.ui.theme.MemoryColor
import com.example.ui.theme.ReactionColor
import com.example.ui.theme.SpatialColor

enum class GameCategory(
  val id: String,
  val displayName: String,
  val color: Color
) {
  MATH("math", "Mathematics", MathColor),
  MEMORY("memory", "Memory", MemoryColor),
  LOGIC("logic", "Logic & Reasoning", LogicColor),
  ATTENTION("attention", "Attention", AttentionColor),
  REACTION("reaction", "Reaction & Speed", ReactionColor),
  SPATIAL("spatial", "Spatial Reasoning", SpatialColor)
}

enum class DifficultyTier(val levelIndex: Int, val title: String, val multiplier: Float) {
  BEGINNER(1, "Beginner", 1.0f),
  EASY(2, "Easy", 1.2f),
  EASY_PLUS(3, "Easy+", 1.4f),
  NORMAL(4, "Normal", 1.6f),
  INTERMEDIATE(5, "Intermediate", 1.8f),
  ADVANCED(6, "Advanced", 2.0f),
  HARD(7, "Hard", 2.3f),
  EXPERT(8, "Expert", 2.6f),
  MASTER(9, "Master", 3.0f),
  GRANDMASTER(10, "Grandmaster", 3.5f);

  companion object {
    fun fromLevelNumber(level: Int, totalLevels: Int): DifficultyTier {
      val fraction = (level - 1).toFloat() / totalLevels.coerceAtLeast(1)
      val tierIdx = (fraction * entries.size).toInt().coerceIn(0, entries.size - 1)
      return entries[tierIdx]
    }
  }
}

data class GameDefinition(
  val id: String,
  val title: String,
  val category: GameCategory,
  val description: String,
  val totalLevels: Int = 50,
  val iconEmoji: String = "🧠",
  val estimatedTimePerLevelSec: Int = 60
)

data class LevelConfig(
  val gameId: String,
  val levelNumber: Int,
  val difficultyTier: DifficultyTier,
  val seed: Long,
  val timeLimitSec: Int = 45,
  val targetQuestions: Int = 5,
  val xpReward: Int = 50
)

data class GameResult(
  val gameId: String,
  val levelNumber: Int,
  val score: Int,
  val accuracy: Int,
  val durationMs: Long,
  val correctAnswers: Int,
  val totalAnswers: Int,
  val isCompleted: Boolean,
  val stars: Int,
  val xpEarned: Int,
  val isNewHighScore: Boolean = false
)
