package com.example

import com.example.core.gameengine.CatalogProvider
import com.example.core.gameengine.LevelGenerators
import com.example.core.model.DifficultyTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MindForgeCoreTest {

  @Test
  fun testTotalLevelsCountIsAtLeast1000() {
    val games = CatalogProvider.games
    assertEquals("Must have at least 20 distinct game modes", 20, games.size)
    val totalLevels = CatalogProvider.totalLevelsCount
    assertTrue("Must have at least 1,000 playable levels, actual: $totalLevels", totalLevels >= 1000)
    assertEquals(1000, totalLevels)
  }

  @Test
  fun testDifficultyTierScaling() {
    val tier1 = DifficultyTier.fromLevelNumber(1, 100)
    val tier50 = DifficultyTier.fromLevelNumber(50, 100)
    val tier100 = DifficultyTier.fromLevelNumber(100, 100)

    assertEquals(DifficultyTier.BEGINNER, tier1)
    assertTrue(tier50.levelIndex > tier1.levelIndex)
    assertEquals(DifficultyTier.GRANDMASTER, tier100)
  }

  @Test
  fun testAllGamesGenerateValidQuestions() {
    for (game in CatalogProvider.games) {
      val config = CatalogProvider.getLevelConfig(game.id, 1)
      assertNotNull(config)
      assertEquals(game.id, config.gameId)
      assertEquals(1, config.levelNumber)

      when (game.id) {
        "memory_match" -> {
          val deck = LevelGenerators.generateMemoryDeck(config)
          assertTrue(deck.isNotEmpty())
          assertEquals(0, deck.size % 2) // Must be pairs
        }
        "visual_grid_memory" -> {
          val puzzle = LevelGenerators.generateGridMemory(config)
          assertTrue(puzzle.gridSize in 3..4)
          assertTrue(puzzle.highlightedIndices.isNotEmpty())
        }
        "sudoku" -> {
          val cells = LevelGenerators.generateSudoku(config)
          assertEquals(16, cells.size) // 4x4
          // Check that solution has no row or column duplicates
          for (r in 0..3) {
            val rowVals = cells.filter { it.row == r }.map { it.solutionValue }
            assertEquals(setOf(1, 2, 3, 4), rowVals.toSet())
          }
          for (c in 0..3) {
            val colVals = cells.filter { it.col == c }.map { it.solutionValue }
            assertEquals(setOf(1, 2, 3, 4), colVals.toSet())
          }
        }
        else -> {
          val questions = LevelGenerators.generateQuestions(config)
          assertTrue("Questions must be generated for ${game.id}", questions.isNotEmpty())
          for (q in questions) {
            assertTrue(q.prompt.isNotBlank())
            assertTrue(q.options.isNotEmpty())
            assertTrue(q.correctIndex in q.options.indices)
          }
        }
      }
    }
  }

  @Test
  fun testDeterministicGeneration() {
    val config1 = CatalogProvider.getLevelConfig("mental_arithmetic", 5)
    val config2 = CatalogProvider.getLevelConfig("mental_arithmetic", 5)
    assertEquals(config1.seed, config2.seed)

    val questions1 = LevelGenerators.generateQuestions(config1)
    val questions2 = LevelGenerators.generateQuestions(config2)
    assertEquals(questions1.size, questions2.size)
    for (i in questions1.indices) {
      assertEquals(questions1[i].prompt, questions2[i].prompt)
      assertEquals(questions1[i].options, questions2[i].options)
      assertEquals(questions1[i].correctIndex, questions2[i].correctIndex)
    }
  }
}
