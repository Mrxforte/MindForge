package com.example.core.gameengine

import com.example.core.model.DifficultyTier
import com.example.core.model.LevelConfig
import kotlin.random.Random

object LevelGenerators {

  fun generateQuestions(config: LevelConfig): List<QuestionChoice> {
    val rng = Random(config.seed)
    val count = config.targetQuestions
    return (0 until count).map { index ->
      when (config.gameId) {
        "mental_arithmetic" -> generateArithmetic(config.difficultyTier, rng)
        "number_sequence" -> generateSequence(config.difficultyTier, rng)
        "equation_master" -> generateEquation(config.difficultyTier, rng)
        "number_comparison" -> generateComparison(config.difficultyTier, rng)
        "math_sprint" -> generateSprint(config.difficultyTier, rng)
        "fraction_percentage" -> generateFractionPercentage(config.difficultyTier, rng)
        "number_memory" -> generateNumberMemory(config.difficultyTier, rng)
        "pattern_recall" -> generatePatternRecall(config.difficultyTier, rng)
        "object_recall" -> generateObjectRecall(config.difficultyTier, rng)
        "logic_grid" -> generateLogicDeduction(config.difficultyTier, rng)
        "pattern_matrix" -> generatePatternMatrix(config.difficultyTier, rng)
        "odd_one_out" -> generateOddOneOut(config.difficultyTier, rng)
        "visual_search" -> generateVisualSearch(config.difficultyTier, rng)
        "stroop_challenge" -> generateStroop(rng)
        "direction_challenge" -> generateDirection(config.difficultyTier, rng)
        else -> generateArithmetic(config.difficultyTier, rng)
      }
    }
  }

  // 1. Mental Arithmetic
  private fun generateArithmetic(tier: DifficultyTier, rng: Random): QuestionChoice {
    val maxVal = when (tier) {
      DifficultyTier.BEGINNER -> 12
      DifficultyTier.EASY, DifficultyTier.EASY_PLUS -> 25
      DifficultyTier.NORMAL, DifficultyTier.INTERMEDIATE -> 60
      DifficultyTier.ADVANCED, DifficultyTier.HARD -> 120
      else -> 250
    }

    val opType = rng.nextInt(if (tier.levelIndex < 3) 2 else 4) // 0: +, 1: -, 2: *, 3: /
    val a: Int
    val b: Int
    val result: Int
    val prompt: String

    when (opType) {
      0 -> {
        a = rng.nextInt(2, maxVal)
        b = rng.nextInt(2, maxVal)
        result = a + b
        prompt = "$a + $b = ?"
      }
      1 -> {
        val x = rng.nextInt(2, maxVal)
        val y = rng.nextInt(2, maxVal)
        a = maxOf(x, y)
        b = minOf(x, y)
        result = a - b
        prompt = "$a - $b = ?"
      }
      2 -> {
        val factorMax = if (tier.levelIndex < 5) 10 else 16
        a = rng.nextInt(2, factorMax)
        b = rng.nextInt(2, factorMax)
        result = a * b
        prompt = "$a × $b = ?"
      }
      else -> {
        val divisorMax = if (tier.levelIndex < 5) 9 else 15
        b = rng.nextInt(2, divisorMax)
        result = rng.nextInt(2, divisorMax)
        a = b * result
        prompt = "$a ÷ $b = ?"
      }
    }

    val options = generateDistractors(result, rng)
    return QuestionChoice(
      prompt = prompt,
      subtitle = "Calculate the exact result",
      options = options.map { it.toString() },
      correctIndex = options.indexOf(result)
    )
  }

  // 2. Number Sequence
  private fun generateSequence(tier: DifficultyTier, rng: Random): QuestionChoice {
    val step = rng.nextInt(2, 4 + tier.levelIndex)
    val start = rng.nextInt(1, 20)
    val type = rng.nextInt(if (tier.levelIndex < 4) 2 else 4)

    val list = mutableListOf<Int>()
    when (type) {
      0 -> { // Arithmetic sequence (start, start+s, start+2s...)
        for (i in 0..4) list.add(start + i * step)
      }
      1 -> { // Decreasing arithmetic
        val base = start + 5 * step
        for (i in 0..4) list.add(base - i * step)
      }
      2 -> { // Geometric progression
        val factor = rng.nextInt(2, 4)
        var curr = rng.nextInt(1, 4)
        for (i in 0..4) {
          list.add(curr)
          curr *= factor
        }
      }
      else -> { // Alternating (+step, -1)
        var curr = start
        for (i in 0..4) {
          list.add(curr)
          curr += if (i % 2 == 0) step else -1
        }
      }
    }

    val answer = list.last()
    val displayList = list.dropLast(1).joinToString(", ") + ", ?"
    val options = generateDistractors(answer, rng)

    return QuestionChoice(
      prompt = displayList,
      subtitle = "Identify the missing number",
      options = options.map { it.toString() },
      correctIndex = options.indexOf(answer)
    )
  }

  // 3. Equation Master
  private fun generateEquation(tier: DifficultyTier, rng: Random): QuestionChoice {
    val a = rng.nextInt(3, 15 * tier.levelIndex.coerceAtLeast(1))
    val b = rng.nextInt(2, 12)
    val mode = rng.nextInt(2) // 0: missing operator, 1: missing operand

    if (mode == 0) {
      val op = listOf("+", "-", "×").random(rng)
      val res = when (op) {
        "+" -> a + b
        "-" -> a - b
        else -> a * b
      }
      val options = listOf("+", "-", "×", "÷").shuffled(rng)
      return QuestionChoice(
        prompt = "$a [ ? ] $b = $res",
        subtitle = "Find the missing operator",
        options = options,
        correctIndex = options.indexOf(op)
      )
    } else {
      val c = a + b
      val options = generateDistractors(b, rng)
      return QuestionChoice(
        prompt = "$a + [ ? ] = $c",
        subtitle = "Find the missing value",
        options = options.map { it.toString() },
        correctIndex = options.indexOf(b)
      )
    }
  }

  // 4. Number Comparison
  private fun generateComparison(tier: DifficultyTier, rng: Random): QuestionChoice {
    val val1 = rng.nextInt(10, 50 * tier.levelIndex)
    val val2 = rng.nextInt(10, 50 * tier.levelIndex)
    val isBiggerTarget = rng.nextBoolean()

    val options = if (val1 == val2) {
      listOf("${val1 + 1}", "$val2")
    } else {
      listOf("$val1", "$val2")
    }

    val correct = if (isBiggerTarget) {
      maxOf(options[0].toInt(), options[1].toInt()).toString()
    } else {
      minOf(options[0].toInt(), options[1].toInt()).toString()
    }

    return QuestionChoice(
      prompt = if (isBiggerTarget) "Select the LARGEST value" else "Select the SMALLEST value",
      subtitle = "${options[0]}   vs   ${options[1]}",
      options = options,
      correctIndex = options.indexOf(correct)
    )
  }

  // 5. Math Sprint (True / False rapid verification)
  private fun generateSprint(tier: DifficultyTier, rng: Random): QuestionChoice {
    val a = rng.nextInt(2, 10 + 3 * tier.levelIndex)
    val b = rng.nextInt(2, 10 + 3 * tier.levelIndex)
    val isTrueEquation = rng.nextBoolean()
    val actual = a + b
    val displayResult = if (isTrueEquation) actual else actual + listOf(-2, -1, 1, 2).random(rng)

    return QuestionChoice(
      prompt = "$a + $b = $displayResult",
      subtitle = "Is this equation TRUE or FALSE?",
      options = listOf("TRUE", "FALSE"),
      correctIndex = if (displayResult == actual) 0 else 1
    )
  }

  // 6. Fraction & Percentage
  private fun generateFractionPercentage(tier: DifficultyTier, rng: Random): QuestionChoice {
    val presets = listOf(
      Triple("25% of 80", 20, listOf(15, 20, 25, 30)),
      Triple("50% of 160", 80, listOf(70, 75, 80, 85)),
      Triple("10% of 350", 35, listOf(30, 35, 40, 45)),
      Triple("75% of 40", 30, listOf(25, 28, 30, 32)),
      Triple("20% of 90", 18, listOf(16, 18, 20, 22)),
      Triple("1/4 as percentage", 25, listOf(20, 25, 30, 40)),
      Triple("3/5 as percentage", 60, listOf(50, 55, 60, 65)),
      Triple("1/2 of 96", 48, listOf(44, 46, 48, 52)),
      Triple("15% of 200", 30, listOf(25, 30, 35, 40)),
      Triple("30% of 300", 90, listOf(80, 85, 90, 95))
    )
    val item = presets.random(rng)
    val opts = item.third.shuffled(rng)
    return QuestionChoice(
      prompt = item.first,
      subtitle = "Calculate the equivalent value",
      options = opts.map { it.toString() },
      correctIndex = opts.indexOf(item.second)
    )
  }

  // 9. Number Memory
  private fun generateNumberMemory(tier: DifficultyTier, rng: Random): QuestionChoice {
    val digitsCount = (3 + tier.levelIndex / 2).coerceIn(4, 8)
    val min = (Math.pow(10.0, (digitsCount - 1).toDouble())).toLong()
    val max = (Math.pow(10.0, digitsCount.toDouble()) - 1).toLong()
    val num = rng.nextLong(min, max)

    val distractors = mutableListOf(num)
    while (distractors.size < 4) {
      val delta = listOf(-10L, 10L, -100L, 100L, -1L, 1L, -11L, 11L).random(rng)
      val fake = (num + delta).coerceIn(min, max)
      if (fake !in distractors) distractors.add(fake)
    }
    distractors.shuffle(rng)

    return QuestionChoice(
      prompt = "$num",
      subtitle = "Memorize this number, then select it!",
      options = distractors.map { it.toString() },
      correctIndex = distractors.indexOf(num)
    )
  }

  // 11. Pattern Recall
  private fun generatePatternRecall(tier: DifficultyTier, rng: Random): QuestionChoice {
    val shapes = listOf("🔵 Blue Circle", "🔺 Red Triangle", "🟩 Green Square", "⭐ Gold Star", "🔷 Cyan Diamond")
    val seq = shapes.shuffled(rng).take(3 + (tier.levelIndex / 3).coerceAtMost(2))
    val targetPos = rng.nextInt(seq.size)
    val answer = seq[targetPos]
    val options = shapes.shuffled(rng).take(4).let { if (answer !in it) (it.dropLast(1) + answer).shuffled(rng) else it }

    return QuestionChoice(
      prompt = seq.joinToString("  →  "),
      subtitle = "What was item #${targetPos + 1}?",
      options = options,
      correctIndex = options.indexOf(answer)
    )
  }

  // 12. Object Recall
  private fun generateObjectRecall(tier: DifficultyTier, rng: Random): QuestionChoice {
    val allObjects = listOf("🍎 Apple", "🚀 Rocket", "🎸 Guitar", "⚽ Ball", "🔑 Key", "📚 Books", "💡 Bulb", "☕ Coffee", "👑 Crown", "🚲 Bike")
    val selected = allObjects.shuffled(rng).take(4 + (tier.levelIndex / 3).coerceAtMost(3))
    val correctItem = selected.random(rng)
    val notInList = allObjects.filter { it !in selected }.shuffled(rng).take(3)
    val options = (notInList + correctItem).shuffled(rng)

    return QuestionChoice(
      prompt = selected.joinToString("  "),
      subtitle = "Which of these objects was in the list?",
      options = options,
      correctIndex = options.indexOf(correctItem)
    )
  }

  // 13. Logic Deduction
  private fun generateLogicDeduction(tier: DifficultyTier, rng: Random): QuestionChoice {
    val names = listOf("Alex", "Boris", "Claire", "Diana").shuffled(rng)
    val puzzles = listOf(
      Triple("${names[0]} is faster than ${names[1]}. ${names[1]} is faster than ${names[2]}.", "Who is FASTEST?", names[0]),
      Triple("${names[0]} is taller than ${names[1]}. ${names[1]} is taller than ${names[2]}.", "Who is SHORTEST?", names[2]),
      Triple("The Red box is heavier than the Blue box. The Green box is lighter than the Blue box.", "Which box is HEAVIEST?", "Red box"),
      Triple("${names[0]} finished before ${names[1]}. ${names[2]} finished after ${names[1]}.", "Who finished FIRST?", names[0]),
      Triple("A is older than B. C is younger than B.", "Who is the OLDEST?", "A")
    )
    val p = puzzles.random(rng)
    val opts = when (p.third) {
      "Red box" -> listOf("Red box", "Blue box", "Green box").shuffled(rng)
      "A" -> listOf("A", "B", "C").shuffled(rng)
      else -> listOf(names[0], names[1], names[2]).shuffled(rng)
    }

    return QuestionChoice(
      prompt = p.first,
      subtitle = p.second,
      options = opts,
      correctIndex = opts.indexOf(p.third)
    )
  }

  private data class MatrixItem(val prompt: String, val subtitle: String, val answer: String, val options: List<String>)

  // 15. Pattern Matrix
  private fun generatePatternMatrix(tier: DifficultyTier, rng: Random): QuestionChoice {
    val patterns = listOf(
      MatrixItem("▲  ▲▲  ▲▲▲\n●  ●●  ?", "What completes the row?", "●●●", listOf("●", "●●", "●●●", "■■")),
      MatrixItem("1  2  3\n4  5  ?", "Find the missing number", "6", listOf("5", "6", "7", "8")),
      MatrixItem("□  ■  □\n■  □  ?", "Find the alternating square", "■", listOf("□", "■", "▲", "●")),
      MatrixItem("A  B  C\nD  E  ?", "Alphabetical sequence", "F", listOf("E", "F", "G", "H")),
      MatrixItem("2  4  8\n3  6  ?", "Double the value rule", "12", listOf("9", "10", "12", "15"))
    )
    val chosen = patterns.random(rng)
    val opts = chosen.options.shuffled(rng)
    return QuestionChoice(
      prompt = chosen.prompt,
      subtitle = chosen.subtitle,
      options = opts,
      correctIndex = opts.indexOf(chosen.answer)
    )
  }

  // 16. Odd One Out
  private fun generateOddOneOut(tier: DifficultyTier, rng: Random): QuestionChoice {
    val sets = listOf(
      Pair(listOf("12", "14", "16", "17"), 3), // 17 is odd
      Pair(listOf("2", "3", "5", "9"), 3), // 9 is composite
      Pair(listOf("Cat", "Dog", "Wolf", "Eagle"), 3), // Eagle is bird
      Pair(listOf("Circle", "Oval", "Sphere", "Square"), 3), // Square has corners
      Pair(listOf("10", "20", "30", "35"), 3), // not multiple of 10
      Pair(listOf("Red", "Blue", "Green", "Heavy"), 3), // Heavy is not a color
      Pair(listOf("January", "March", "May", "April"), 3), // April has 30 days
      Pair(listOf("25", "36", "49", "50"), 3) // 50 is not a perfect square
    )
    val selected = sets.random(rng)
    val items = selected.first
    val oddIndex = selected.second
    val target = items[oddIndex]
    val shuffled = items.shuffled(rng)

    return QuestionChoice(
      prompt = shuffled.joinToString("   |   "),
      subtitle = "Tap the item that DOES NOT belong",
      options = shuffled,
      correctIndex = shuffled.indexOf(target)
    )
  }

  // 17. Visual Search
  private fun generateVisualSearch(tier: DifficultyTier, rng: Random): QuestionChoice {
    val pairs = listOf(
      Pair("Q", "O"),
      Pair("F", "E"),
      Pair("6", "9"),
      Pair("💎", "🔷"),
      Pair("⭐", "✨")
    )
    val pair = pairs.random(rng)
    val target = pair.first
    val distractor = pair.second

    val size = 16 // 4x4 matrix
    val targetPos = rng.nextInt(size)
    val gridItems = (0 until size).map { if (it == targetPos) target else distractor }

    // Represent as 4 options of quadrant/locations or prompt
    return QuestionChoice(
      prompt = "Target: $target\n\n" + gridItems.chunked(4).joinToString("\n") { it.joinToString(" ") },
      subtitle = "Which row is the target '$target' in?",
      options = listOf("Row 1", "Row 2", "Row 3", "Row 4"),
      correctIndex = targetPos / 4
    )
  }

  // 18. Stroop Challenge
  private fun generateStroop(rng: Random): QuestionChoice {
    val colors = StroopColor.entries
    val wordColor = colors.random(rng)
    val inkColor = colors.random(rng) // May or may not match

    val options = colors.map { it.displayName }.shuffled(rng)
    return QuestionChoice(
      prompt = wordColor.displayName, // Text word
      subtitle = "${inkColor.hexCode}", // We store the ink hex in subtitle for UI rendering!
      options = options,
      correctIndex = options.indexOf(inkColor.displayName),
      explanation = "Identify the INK COLOR, ignore the written word!"
    )
  }

  // 20. Direction Challenge
  private fun generateDirection(tier: DifficultyTier, rng: Random): QuestionChoice {
    val arrows = listOf("⬆️ UP", "⬇️ DOWN", "⬅️ LEFT", "➡️ RIGHT")
    val chosenArrow = arrows.random(rng)
    val isOpposite = rng.nextBoolean()

    val correct = if (isOpposite) {
      when (chosenArrow) {
        "⬆️ UP" -> "⬇️ DOWN"
        "⬇️ DOWN" -> "⬆️ UP"
        "⬅️ LEFT" -> "➡️ RIGHT"
        else -> "⬅️ LEFT"
      }
    } else {
      chosenArrow
    }

    return QuestionChoice(
      prompt = chosenArrow,
      subtitle = if (isOpposite) "⚠️ TAP OPPOSITE DIRECTION" else "✅ TAP MATCHING DIRECTION",
      options = arrows,
      correctIndex = arrows.indexOf(correct)
    )
  }

  // 7. Memory Match deck generator
  fun generateMemoryDeck(config: LevelConfig): List<MemoryCard> {
    val rng = Random(config.seed)
    val pairCount = when (config.difficultyTier) {
      DifficultyTier.BEGINNER, DifficultyTier.EASY -> 4 // 8 cards (2x4 or 4x2)
      DifficultyTier.EASY_PLUS, DifficultyTier.NORMAL -> 6 // 12 cards (3x4)
      else -> 8 // 16 cards (4x4)
    }

    val symbols = listOf("⚡", "🔥", "💎", "⭐", "🔮", "🍀", "🚀", "🪐", "🎯", "🛡️", "🧬", "👑").shuffled(rng)
    val chosen = symbols.take(pairCount)
    val cards = (chosen + chosen).shuffled(rng).mapIndexed { idx, sym ->
      MemoryCard(id = idx, symbol = sym)
    }
    return cards
  }

  // 10. Visual Grid Memory generator
  fun generateGridMemory(config: LevelConfig): GridMemoryPuzzle {
    val rng = Random(config.seed)
    val gridSize = if (config.difficultyTier.levelIndex < 5) 3 else 4
    val totalCells = gridSize * gridSize
    val activeCount = (3 + config.difficultyTier.levelIndex / 2).coerceIn(3, totalCells - 2)

    val indices = (0 until totalCells).shuffled(rng).take(activeCount).toSet()
    return GridMemoryPuzzle(
      gridSize = gridSize,
      highlightedIndices = indices,
      showDurationMs = 2000L
    )
  }

  // 14. Sudoku 4x4 Mini Board generator (Solvable & validated!)
  fun generateSudoku(config: LevelConfig): List<SudokuCell> {
    val rng = Random(config.seed)
    // Valid 4x4 latin square base
    val solution = arrayOf(
      intArrayOf(1, 2, 3, 4),
      intArrayOf(3, 4, 1, 2),
      intArrayOf(2, 1, 4, 3),
      intArrayOf(4, 3, 2, 1)
    )
    // Permute numbers
    val map = (1..4).shuffled(rng)
    for (r in 0..3) {
      for (c in 0..3) {
        solution[r][c] = map[solution[r][c] - 1]
      }
    }

    val cells = mutableListOf<SudokuCell>()
    // Clues to give (e.g., 8-10 given out of 16)
    val givensCount = (10 - (config.difficultyTier.levelIndex / 2)).coerceIn(6, 11)
    val givenIndices = (0..15).shuffled(rng).take(givensCount).toSet()

    for (r in 0..3) {
      for (c in 0..3) {
        val idx = r * 4 + c
        val isGiven = idx in givenIndices
        val sol = solution[r][c]
        cells.add(
          SudokuCell(
            row = r,
            col = c,
            value = if (isGiven) sol else 0,
            isGiven = isGiven,
            solutionValue = sol
          )
        )
      }
    }
    return cells
  }

  private fun generateDistractors(correct: Int, rng: Random): List<Int> {
    val set = mutableSetOf(correct)
    val offsets = listOf(-3, -2, -1, 1, 2, 3, 4, 5, 10, -10).shuffled(rng)
    for (offset in offsets) {
      val candidate = correct + offset
      if (candidate > 0) set.add(candidate)
      if (set.size == 4) break
    }
    while (set.size < 4) {
      set.add(correct + set.size * 2)
    }
    return set.toList().shuffled(rng)
  }
}
