package com.example.core.gameengine

data class QuestionChoice(
  val prompt: String,
  val subtitle: String = "",
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String = ""
)

data class GridMemoryPuzzle(
  val gridSize: Int, // e.g. 3 for 3x3, 4 for 4x4
  val highlightedIndices: Set<Int>,
  val showDurationMs: Long = 2000L
)

data class MemoryCard(
  val id: Int,
  val symbol: String,
  val isFaceUp: Boolean = false,
  val isMatched: Boolean = false
)

data class SudokuCell(
  val row: Int,
  val col: Int,
  val value: Int,
  val isGiven: Boolean,
  val solutionValue: Int
)

enum class StroopColor(val displayName: String, val hexCode: Long) {
  RED("RED", 0xFFEF4444),
  BLUE("BLUE", 0xFF3B82F6),
  GREEN("GREEN", 0xFF10B981),
  YELLOW("YELLOW", 0xFFF59E0B),
  PURPLE("PURPLE", 0xFF8B5CF6);
}
