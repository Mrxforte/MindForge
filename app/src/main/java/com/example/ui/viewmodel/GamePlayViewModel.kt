package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.database.AppDatabase
import com.example.core.data.repository.GameRepository
import com.example.core.gameengine.CatalogProvider
import com.example.core.gameengine.GridMemoryPuzzle
import com.example.core.gameengine.LevelGenerators
import com.example.core.gameengine.MemoryCard
import com.example.core.gameengine.QuestionChoice
import com.example.core.gameengine.SudokuCell
import com.example.core.model.GameResult
import com.example.core.model.LevelConfig
import com.example.core.sound.SoundAndHapticManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GamePlayUiState(
  val config: LevelConfig,
  val currentRound: Int = 1,
  val totalRounds: Int = 5,
  val remainingSeconds: Int = 45,
  val score: Int = 0,
  val combo: Int = 1,
  val correctAnswersCount: Int = 0,
  val totalAnsweredCount: Int = 0,
  val isPaused: Boolean = false,
  val isGameOver: Boolean = false,
  val result: GameResult? = null,

  // Multiple choice state
  val currentQuestion: QuestionChoice? = null,
  val selectedOptionIndex: Int? = null,

  // Memory Match state
  val memoryCards: List<MemoryCard> = emptyList(),

  // Visual Grid Memory state
  val gridPuzzle: GridMemoryPuzzle? = null,
  val isGridMemorizingPhase: Boolean = true,
  val gridSelectedIndices: Set<Int> = emptySet(),

  // Sequence Recall state
  val sequenceList: List<Int> = emptyList(),
  val activeFlashPad: Int? = null,
  val isSequenceInputAllowed: Boolean = false,
  val sequenceUserStep: Int = 0,

  // Reaction Timer state
  val reactionState: ReactionPhase = ReactionPhase.WAITING,
  val reactionRecordedMs: Long = 0L,

  // Sudoku state
  val sudokuCells: List<SudokuCell> = emptyList(),
  val sudokuSelectedCell: Pair<Int, Int>? = null
)

enum class ReactionPhase {
  WAITING,
  READY_TO_TAP,
  TOO_EARLY,
  COMPLETED_TRIAL
}

class GamePlayViewModel(
  application: Application,
  val gameId: String,
  val levelNumber: Int,
  private val soundEnabled: Boolean = true,
  private val hapticsEnabled: Boolean = true
) : AndroidViewModel(application) {

  private val repository = GameRepository(AppDatabase.getInstance(application))
  private val soundManager = SoundAndHapticManager(application)

  private val levelConfig = CatalogProvider.getLevelConfig(gameId, levelNumber)

  private val _uiState = MutableStateFlow(
    GamePlayUiState(
      config = levelConfig,
      totalRounds = levelConfig.targetQuestions,
      remainingSeconds = levelConfig.timeLimitSec
    )
  )
  val uiState: StateFlow<GamePlayUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null
  private var questionsList: List<QuestionChoice> = emptyList()
  private var startTimeMs: Long = 0L
  private var reactionStartTimestamp: Long = 0L
  private var firstFlippedCard: MemoryCard? = null

  init {
    startTimeMs = System.currentTimeMillis()
    initializeGame()
    startTimer()
  }

  private fun initializeGame() {
    when (gameId) {
      "memory_match" -> {
        val cards = LevelGenerators.generateMemoryDeck(levelConfig)
        _uiState.value = _uiState.value.copy(
          memoryCards = cards,
          totalRounds = cards.size / 2
        )
      }
      "visual_grid_memory" -> {
        startGridMemoryRound()
      }
      "sequence_recall" -> {
        startSequenceRecallRound(round = 1)
      }
      "reaction_timer" -> {
        startReactionTrial()
      }
      "sudoku" -> {
        val cells = LevelGenerators.generateSudoku(levelConfig)
        _uiState.value = _uiState.value.copy(
          sudokuCells = cells,
          totalRounds = 1
        )
      }
      else -> {
        questionsList = LevelGenerators.generateQuestions(levelConfig)
        _uiState.value = _uiState.value.copy(
          currentQuestion = questionsList.firstOrNull(),
          totalRounds = questionsList.size
        )
      }
    }
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isGameOver) {
        delay(1000L)
        if (!_uiState.value.isPaused) {
          val remaining = _uiState.value.remainingSeconds - 1
          _uiState.value = _uiState.value.copy(remainingSeconds = remaining)
          if (remaining <= 0) {
            finishGame(isTimeout = true)
          }
        }
      }
    }
  }

  fun togglePause() {
    _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
  }

  // Handle standard Multiple Choice answer
  fun submitAnswer(optionIndex: Int) {
    if (_uiState.value.selectedOptionIndex != null || _uiState.value.isGameOver) return
    val q = _uiState.value.currentQuestion ?: return
    val isCorrect = optionIndex == q.correctIndex

    val basePoints = 100 * levelConfig.difficultyTier.multiplier.toInt().coerceAtLeast(1)
    val earnedPoints = if (isCorrect) basePoints * _uiState.value.combo else 0

    if (isCorrect) {
      soundManager.playCorrect(soundEnabled, hapticsEnabled)
    } else {
      soundManager.playIncorrect(soundEnabled, hapticsEnabled)
    }

    _uiState.value = _uiState.value.copy(
      selectedOptionIndex = optionIndex,
      score = _uiState.value.score + earnedPoints,
      combo = if (isCorrect) _uiState.value.combo + 1 else 1,
      correctAnswersCount = _uiState.value.correctAnswersCount + (if (isCorrect) 1 else 0),
      totalAnsweredCount = _uiState.value.totalAnsweredCount + 1
    )

    viewModelScope.launch {
      delay(400L)
      if (_uiState.value.currentRound < _uiState.value.totalRounds) {
        val nextRound = _uiState.value.currentRound + 1
        _uiState.value = _uiState.value.copy(
          currentRound = nextRound,
          currentQuestion = questionsList.getOrNull(nextRound - 1),
          selectedOptionIndex = null
        )
      } else {
        finishGame(isTimeout = false)
      }
    }
  }

  // Memory Match card flip
  fun onCardClick(card: MemoryCard) {
    val currentCards = _uiState.value.memoryCards.toMutableList()
    val cardIndex = currentCards.indexOfFirst { it.id == card.id }
    if (cardIndex == -1 || currentCards[cardIndex].isFaceUp || currentCards[cardIndex].isMatched) return

    soundManager.playClick(soundEnabled, hapticsEnabled)
    currentCards[cardIndex] = currentCards[cardIndex].copy(isFaceUp = true)
    _uiState.value = _uiState.value.copy(memoryCards = currentCards)

    if (firstFlippedCard == null) {
      firstFlippedCard = currentCards[cardIndex]
    } else {
      val first = firstFlippedCard!!
      val second = currentCards[cardIndex]
      firstFlippedCard = null

      if (first.symbol == second.symbol) {
        soundManager.playCorrect(soundEnabled, hapticsEnabled)
        val matchedCards = currentCards.map {
          if (it.id == first.id || it.id == second.id) it.copy(isMatched = true) else it
        }
        val newCorrectCount = _uiState.value.correctAnswersCount + 1
        val newScore = _uiState.value.score + 150 * _uiState.value.combo
        _uiState.value = _uiState.value.copy(
          memoryCards = matchedCards,
          correctAnswersCount = newCorrectCount,
          score = newScore,
          combo = _uiState.value.combo + 1
        )
        if (matchedCards.all { it.isMatched }) {
          finishGame(isTimeout = false)
        }
      } else {
        soundManager.playIncorrect(soundEnabled, hapticsEnabled)
        _uiState.value = _uiState.value.copy(combo = 1)
        viewModelScope.launch {
          delay(600L)
          val resetCards = _uiState.value.memoryCards.map {
            if (it.id == first.id || it.id == second.id) it.copy(isFaceUp = false) else it
          }
          _uiState.value = _uiState.value.copy(memoryCards = resetCards)
        }
      }
    }
  }

  // Visual Grid Memory
  private fun startGridMemoryRound() {
    val puzzle = LevelGenerators.generateGridMemory(levelConfig)
    _uiState.value = _uiState.value.copy(
      gridPuzzle = puzzle,
      isGridMemorizingPhase = true,
      gridSelectedIndices = emptySet()
    )
    viewModelScope.launch {
      delay(puzzle.showDurationMs)
      _uiState.value = _uiState.value.copy(isGridMemorizingPhase = false)
    }
  }

  fun onGridCellClick(index: Int) {
    val puzzle = _uiState.value.gridPuzzle ?: return
    if (_uiState.value.isGridMemorizingPhase) return

    val currentSelected = _uiState.value.gridSelectedIndices.toMutableSet()
    if (index in currentSelected) return
    currentSelected.add(index)
    soundManager.playClick(soundEnabled, hapticsEnabled)

    val isCorrectTile = index in puzzle.highlightedIndices
    if (isCorrectTile) {
      soundManager.playCorrect(soundEnabled, hapticsEnabled)
      _uiState.value = _uiState.value.copy(
        gridSelectedIndices = currentSelected,
        score = _uiState.value.score + 100
      )
      // Check if all highlighted tiles found
      val foundAll = puzzle.highlightedIndices.all { it in currentSelected }
      if (foundAll) {
        val nextRound = _uiState.value.currentRound + 1
        if (nextRound <= _uiState.value.totalRounds) {
          _uiState.value = _uiState.value.copy(
            currentRound = nextRound,
            correctAnswersCount = _uiState.value.correctAnswersCount + 1
          )
          startGridMemoryRound()
        } else {
          finishGame(isTimeout = false)
        }
      }
    } else {
      soundManager.playIncorrect(soundEnabled, hapticsEnabled)
      _uiState.value = _uiState.value.copy(gridSelectedIndices = currentSelected)
    }
  }

  // Sequence Recall
  private fun startSequenceRecallRound(round: Int) {
    val len = 2 + round
    val rng = kotlin.random.Random(levelConfig.seed + round * 17)
    val seq = (0 until len).map { rng.nextInt(4) }
    _uiState.value = _uiState.value.copy(
      sequenceList = seq,
      isSequenceInputAllowed = false,
      sequenceUserStep = 0,
      activeFlashPad = null
    )
    // Flash sequence
    viewModelScope.launch {
      delay(600L)
      for (pad in seq) {
        _uiState.value = _uiState.value.copy(activeFlashPad = pad)
        soundManager.playClick(soundEnabled, hapticsEnabled)
        delay(400L)
        _uiState.value = _uiState.value.copy(activeFlashPad = null)
        delay(200L)
      }
      _uiState.value = _uiState.value.copy(isSequenceInputAllowed = true)
    }
  }

  fun onSequencePadClick(padIndex: Int) {
    if (!_uiState.value.isSequenceInputAllowed) return
    val seq = _uiState.value.sequenceList
    val step = _uiState.value.sequenceUserStep
    val expected = seq.getOrNull(step)

    soundManager.playClick(soundEnabled, hapticsEnabled)
    if (padIndex == expected) {
      val nextStep = step + 1
      if (nextStep >= seq.size) {
        soundManager.playCorrect(soundEnabled, hapticsEnabled)
        val nextRound = _uiState.value.currentRound + 1
        _uiState.value = _uiState.value.copy(
          score = _uiState.value.score + 150 * nextRound,
          correctAnswersCount = _uiState.value.correctAnswersCount + 1
        )
        if (nextRound <= _uiState.value.totalRounds) {
          _uiState.value = _uiState.value.copy(currentRound = nextRound)
          startSequenceRecallRound(nextRound)
        } else {
          finishGame(isTimeout = false)
        }
      } else {
        _uiState.value = _uiState.value.copy(sequenceUserStep = nextStep)
      }
    } else {
      soundManager.playIncorrect(soundEnabled, hapticsEnabled)
      // Repeat the same round
      startSequenceRecallRound(_uiState.value.currentRound)
    }
  }

  // Reaction Timer
  private fun startReactionTrial() {
    _uiState.value = _uiState.value.copy(
      reactionState = ReactionPhase.WAITING,
      reactionRecordedMs = 0L
    )
    val delayMs = 1500L + (kotlin.random.Random.nextLong(2500L))
    viewModelScope.launch {
      delay(delayMs)
      if (_uiState.value.reactionState == ReactionPhase.WAITING) {
        reactionStartTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(reactionState = ReactionPhase.READY_TO_TAP)
        soundManager.playClick(soundEnabled, hapticsEnabled)
      }
    }
  }

  fun onReactionTap() {
    val phase = _uiState.value.reactionState
    if (phase == ReactionPhase.WAITING) {
      soundManager.playIncorrect(soundEnabled, hapticsEnabled)
      _uiState.value = _uiState.value.copy(reactionState = ReactionPhase.TOO_EARLY)
      viewModelScope.launch {
        delay(1200L)
        startReactionTrial()
      }
    } else if (phase == ReactionPhase.READY_TO_TAP) {
      val reactionTime = System.currentTimeMillis() - reactionStartTimestamp
      soundManager.playCorrect(soundEnabled, hapticsEnabled)
      val trialScore = (500 - reactionTime).coerceAtLeast(50).toInt()
      val nextRound = _uiState.value.currentRound + 1
      _uiState.value = _uiState.value.copy(
        reactionState = ReactionPhase.COMPLETED_TRIAL,
        reactionRecordedMs = reactionTime,
        score = _uiState.value.score + trialScore,
        correctAnswersCount = _uiState.value.correctAnswersCount + 1
      )
      viewModelScope.launch {
        delay(800L)
        if (nextRound <= _uiState.value.totalRounds) {
          _uiState.value = _uiState.value.copy(currentRound = nextRound)
          startReactionTrial()
        } else {
          finishGame(isTimeout = false)
        }
      }
    }
  }

  // Sudoku
  fun selectSudokuCell(row: Int, col: Int) {
    _uiState.value = _uiState.value.copy(sudokuSelectedCell = Pair(row, col))
  }

  fun inputSudokuNumber(num: Int) {
    val sel = _uiState.value.sudokuSelectedCell ?: return
    val current = _uiState.value.sudokuCells.toMutableList()
    val idx = current.indexOfFirst { it.row == sel.first && it.col == sel.second }
    if (idx != -1 && !current[idx].isGiven) {
      current[idx] = current[idx].copy(value = num)
      soundManager.playClick(soundEnabled, hapticsEnabled)
      _uiState.value = _uiState.value.copy(sudokuCells = current)

      // Check if all cells filled and correct
      val allFilled = current.none { it.value == 0 }
      if (allFilled) {
        val allCorrect = current.all { it.value == it.solutionValue }
        if (allCorrect) {
          soundManager.playCorrect(soundEnabled, hapticsEnabled)
          _uiState.value = _uiState.value.copy(score = 500, correctAnswersCount = 1)
          finishGame(isTimeout = false)
        } else {
          soundManager.playIncorrect(soundEnabled, hapticsEnabled)
        }
      }
    }
  }

  private fun finishGame(isTimeout: Boolean) {
    timerJob?.cancel()
    val duration = System.currentTimeMillis() - startTimeMs
    val totalQ = _uiState.value.totalRounds.coerceAtLeast(1)
    val correctQ = _uiState.value.correctAnswersCount
    val accuracy = ((correctQ.toFloat() / totalQ.toFloat()) * 100).toInt().coerceIn(0, 100)
    val isCompleted = !isTimeout && (accuracy >= 60 || gameId in listOf("reaction_timer", "memory_match", "sudoku"))
    val stars = when {
      accuracy >= 90 -> 3
      accuracy >= 75 -> 2
      accuracy >= 60 -> 1
      else -> 0
    }
    val xpEarned = if (isCompleted) levelConfig.xpReward + stars * 10 else 15

    soundManager.playLevelComplete(soundEnabled, hapticsEnabled)

    val gameResult = GameResult(
      gameId = gameId,
      levelNumber = levelNumber,
      score = _uiState.value.score,
      accuracy = accuracy,
      durationMs = duration,
      correctAnswers = correctQ,
      totalAnswers = totalQ,
      isCompleted = isCompleted,
      stars = stars,
      xpEarned = xpEarned
    )

    _uiState.value = _uiState.value.copy(
      isGameOver = true,
      result = gameResult
    )

    viewModelScope.launch {
      val isNewHigh = repository.recordGameCompletion(gameResult)
      _uiState.value = _uiState.value.copy(
        result = gameResult.copy(isNewHighScore = isNewHigh)
      )
    }
  }
}
