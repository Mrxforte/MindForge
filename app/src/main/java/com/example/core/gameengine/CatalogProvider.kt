package com.example.core.gameengine

import com.example.core.model.DifficultyTier
import com.example.core.model.GameCategory
import com.example.core.model.GameDefinition
import com.example.core.model.LevelConfig

object CatalogProvider {

  val games: List<GameDefinition> = listOf(
    // Category: Mathematics (350 levels)
    GameDefinition(
      id = "mental_arithmetic",
      title = "Mental Arithmetic",
      category = GameCategory.MATH,
      description = "Lightning-fast addition, subtraction, multiplication & division with escalating numbers.",
      totalLevels = 100,
      iconEmoji = "➕",
      estimatedTimePerLevelSec = 45
    ),
    GameDefinition(
      id = "number_sequence",
      title = "Number Sequence",
      category = GameCategory.MATH,
      description = "Deduce the missing step in arithmetic, geometric, Fibonacci, and alternating sequences.",
      totalLevels = 60,
      iconEmoji = "🔢",
      estimatedTimePerLevelSec = 50
    ),
    GameDefinition(
      id = "equation_master",
      title = "Equation Master",
      category = GameCategory.MATH,
      description = "Fill in the missing operators and variables to balance complex mathematical equations.",
      totalLevels = 50,
      iconEmoji = "⚖️",
      estimatedTimePerLevelSec = 50
    ),
    GameDefinition(
      id = "number_comparison",
      title = "Number Comparison",
      category = GameCategory.MATH,
      description = "Speed test comparing numerical values, fractions, and expressions under pressure.",
      totalLevels = 50,
      iconEmoji = "⚡",
      estimatedTimePerLevelSec = 40
    ),
    GameDefinition(
      id = "math_sprint",
      title = "Math Sprint",
      category = GameCategory.MATH,
      description = "Rapid-fire rapid arithmetic combo run. Build streaks without breaking momentum.",
      totalLevels = 50,
      iconEmoji = "🏃",
      estimatedTimePerLevelSec = 40
    ),
    GameDefinition(
      id = "fraction_percentage",
      title = "Fraction & Percentage",
      category = GameCategory.MATH,
      description = "Master conversions between fractions, decimals, and percentages with mental shortcuts.",
      totalLevels = 40,
      iconEmoji = "📊",
      estimatedTimePerLevelSec = 50
    ),

    // Category: Memory (300 levels)
    GameDefinition(
      id = "memory_match",
      title = "Memory Match",
      category = GameCategory.MEMORY,
      description = "Flip and match geometric symbol cards on grids scaling from 2x3 to 4x5.",
      totalLevels = 60,
      iconEmoji = "🃏",
      estimatedTimePerLevelSec = 60
    ),
    GameDefinition(
      id = "sequence_recall",
      title = "Sequence Recall",
      category = GameCategory.MEMORY,
      description = "Watch synaptic color pulses and reproduce the exact sequence without error.",
      totalLevels = 60,
      iconEmoji = "🔮",
      estimatedTimePerLevelSec = 50
    ),
    GameDefinition(
      id = "number_memory",
      title = "Number Memory",
      category = GameCategory.MEMORY,
      description = "Briefly observe multi-digit numbers and recall them after disappearance.",
      totalLevels = 50,
      iconEmoji = "🔣",
      estimatedTimePerLevelSec = 45
    ),
    GameDefinition(
      id = "visual_grid_memory",
      title = "Visual Grid Memory",
      category = GameCategory.MEMORY,
      description = "Memorize illuminated tile patterns on an expanding matrix and tap them back.",
      totalLevels = 50,
      iconEmoji = "🗺️",
      estimatedTimePerLevelSec = 45
    ),
    GameDefinition(
      id = "pattern_recall",
      title = "Pattern Recall",
      category = GameCategory.MEMORY,
      description = "Memorize the exact layout of colored geometric shapes and reconstruct them.",
      totalLevels = 40,
      iconEmoji = "💠",
      estimatedTimePerLevelSec = 50
    ),
    GameDefinition(
      id = "object_recall",
      title = "Object Recall",
      category = GameCategory.MEMORY,
      description = "Examine a group of everyday objects, then spot which ones were present.",
      totalLevels = 40,
      iconEmoji = "📦",
      estimatedTimePerLevelSec = 45
    ),

    // Category: Logic & Reasoning (200 levels)
    GameDefinition(
      id = "logic_grid",
      title = "Logic Deduction",
      category = GameCategory.LOGIC,
      description = "Analyze logical propositions and deduce true relationship pairings.",
      totalLevels = 50,
      iconEmoji = "🧩",
      estimatedTimePerLevelSec = 60
    ),
    GameDefinition(
      id = "sudoku",
      title = "Sudoku Forge",
      category = GameCategory.LOGIC,
      description = "Classic numbers logic puzzle. Place numbers so rows and columns contain no repeats.",
      totalLevels = 50,
      iconEmoji = "🎯",
      estimatedTimePerLevelSec = 75
    ),
    GameDefinition(
      id = "pattern_matrix",
      title = "Pattern Matrix",
      category = GameCategory.LOGIC,
      description = "Discover the transformation rule across rows and choose the missing shape.",
      totalLevels = 50,
      iconEmoji = "📐",
      estimatedTimePerLevelSec = 55
    ),
    GameDefinition(
      id = "odd_one_out",
      title = "Odd One Out",
      category = GameCategory.LOGIC,
      description = "Spot the element that does not belong based on hidden mathematical or visual rules.",
      totalLevels = 40,
      iconEmoji = "🔍",
      estimatedTimePerLevelSec = 40
    ),

    // Category: Attention & Concentration (80 levels)
    GameDefinition(
      id = "visual_search",
      title = "Visual Search",
      category = GameCategory.ATTENTION,
      description = "Locate target symbols hidden amongst dense distractor characters against the clock.",
      totalLevels = 40,
      iconEmoji = "👀",
      estimatedTimePerLevelSec = 45
    ),
    GameDefinition(
      id = "stroop_challenge",
      title = "Stroop Challenge",
      category = GameCategory.ATTENTION,
      description = "Overcome cognitive interference: identify the color of the ink, not the written word.",
      totalLevels = 40,
      iconEmoji = "🎨",
      estimatedTimePerLevelSec = 40
    ),

    // Category: Reaction & Speed (40 levels)
    GameDefinition(
      id = "reaction_timer",
      title = "Reaction Reflex",
      category = GameCategory.REACTION,
      description = "Millisecond-precision reaction test. Wait for the green signal and tap instantly.",
      totalLevels = 40,
      iconEmoji = "⏱️",
      estimatedTimePerLevelSec = 35
    ),

    // Category: Spatial Reasoning (40 levels)
    GameDefinition(
      id = "direction_challenge",
      title = "Direction Challenge",
      category = GameCategory.SPATIAL,
      description = "Follow arrow cues with alternating direct and inverted spatial rules.",
      totalLevels = 40,
      iconEmoji = "🧭",
      estimatedTimePerLevelSec = 40
    )
  )

  val totalLevelsCount: Int = games.sumOf { it.totalLevels } // 1,000 levels!

  fun getGameById(id: String): GameDefinition? = games.find { it.id == id }

  fun getLevelConfig(gameId: String, levelNumber: Int): LevelConfig {
    val game = getGameById(gameId) ?: games.first()
    val total = game.totalLevels
    val clampedLevel = levelNumber.coerceIn(1, total)
    val tier = DifficultyTier.fromLevelNumber(clampedLevel, total)
    val seed = (gameId.hashCode().toLong() shl 16) xor (clampedLevel.toLong() * 31337L)
    
    // Scale question count and time limit smoothly with tier
    val baseTime = game.estimatedTimePerLevelSec
    val targetQ = when (gameId) {
      "sudoku" -> 1 // Sudoku is 1 puzzle board
      "reaction_timer" -> 5 // 5 reaction trials
      "memory_match" -> 1 // 1 full grid clear
      "number_memory" -> 4
      "visual_grid_memory" -> 4
      else -> 5 + (clampedLevel / 15).coerceAtMost(5)
    }

    return LevelConfig(
      gameId = gameId,
      levelNumber = clampedLevel,
      difficultyTier = tier,
      seed = seed,
      timeLimitSec = baseTime,
      targetQuestions = targetQ,
      xpReward = 50 + clampedLevel * 5
    )
  }
}
