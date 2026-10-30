package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.gameengine.CatalogProvider
import com.example.ui.components.GameHud
import com.example.ui.components.MemoryMatchBoard
import com.example.ui.components.MultipleChoiceGrid
import com.example.ui.components.SequenceRecallBoard
import com.example.ui.components.SudokuMiniBoard
import com.example.ui.components.VisualGridBoard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.GamePlayUiState
import com.example.ui.viewmodel.GamePlayViewModel
import com.example.ui.viewmodel.ReactionPhase

@Composable
fun GamePlayScreen(
  viewModel: GamePlayViewModel,
  onExit: () -> Unit,
  onNextLevel: (Int) -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  if (uiState.isGameOver && uiState.result != null) {
    ResultScreen(
      result = uiState.result!!,
      onNextLevel = { onNextLevel(uiState.config.levelNumber + 1) },
      onRetry = onRetry,
      onExit = onExit
    )
    return
  }

  Scaffold(
    topBar = {
      GameHud(
        levelNumber = uiState.config.levelNumber,
        currentRound = uiState.currentRound,
        totalRounds = uiState.totalRounds,
        score = uiState.score,
        combo = uiState.combo,
        remainingSeconds = uiState.remainingSeconds,
        totalSeconds = uiState.config.timeLimitSec,
        onPauseClick = { viewModel.togglePause() },
        onExitClick = onExit
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (viewModel.gameId) {
        "memory_match" -> {
          Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Find all matching symbol pairs!",
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            MemoryMatchBoard(
              cards = uiState.memoryCards,
              onCardClick = { viewModel.onCardClick(it) }
            )
          }
        }

        "visual_grid_memory" -> {
          Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = if (uiState.isGridMemorizingPhase) "MEMORIZE THE PATTERN..." else "TAP ALL REMEMBERED TILES!",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = if (uiState.isGridMemorizingPhase) CyanAccent else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            uiState.gridPuzzle?.let { puzzle ->
              VisualGridBoard(
                gridSize = puzzle.gridSize,
                isMemorizingPhase = uiState.isGridMemorizingPhase,
                highlightedIndices = puzzle.highlightedIndices,
                selectedIndices = uiState.gridSelectedIndices,
                onCellClick = { viewModel.onGridCellClick(it) }
              )
            }
          }
        }

        "sequence_recall" -> {
          Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = if (!uiState.isSequenceInputAllowed) "WATCH THE NEURAL SEQUENCE..." else "REPEAT THE SEQUENCE!",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = if (!uiState.isSequenceInputAllowed) WarningOrange else CyanAccent
            )
            Spacer(modifier = Modifier.height(20.dp))
            SequenceRecallBoard(
              activePadIndex = uiState.activeFlashPad,
              onPadClick = { viewModel.onSequencePadClick(it) },
              isInputEnabled = uiState.isSequenceInputAllowed
            )
          }
        }

        "reaction_timer" -> {
          val phase = uiState.reactionState
          val bg = when (phase) {
            ReactionPhase.WAITING -> MaterialTheme.colorScheme.surfaceVariant
            ReactionPhase.READY_TO_TAP -> SuccessGreen
            ReactionPhase.TOO_EARLY -> ErrorRed
            ReactionPhase.COMPLETED_TRIAL -> CyanAccent
          }

          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(bg)
              .clickable { viewModel.onReactionTap() },
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = when (phase) {
                  ReactionPhase.WAITING -> "WAIT FOR GREEN..."
                  ReactionPhase.READY_TO_TAP -> "⚡ TAP NOW! ⚡"
                  ReactionPhase.TOO_EARLY -> "TOO EARLY!"
                  ReactionPhase.COMPLETED_TRIAL -> "${uiState.reactionRecordedMs} ms!"
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (phase == ReactionPhase.WAITING) MaterialTheme.colorScheme.onSurface else Color.White
              )
              if (phase == ReactionPhase.WAITING) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Do not tap until color turns green",
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        "sudoku" -> {
          Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Fill empty squares with 1-4 without repeats",
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            SudokuMiniBoard(
              cells = uiState.sudokuCells,
              selectedCell = uiState.sudokuSelectedCell,
              onCellSelect = { r, c -> viewModel.selectSudokuCell(r, c) },
              onNumberInput = { num -> viewModel.inputSudokuNumber(num) }
            )
          }
        }

        else -> {
          // Standard Question & Multiple Choice flow
          uiState.currentQuestion?.let { q ->
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.height(10.dp))

              // Question Card
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  if (viewModel.gameId == "stroop_challenge") {
                    val inkHex = q.subtitle.toLongOrNull() ?: 0xFFFFFFFF
                    Text(
                      text = q.prompt,
                      fontSize = 38.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(inkHex),
                      textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = q.explanation,
                      fontSize = 14.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center
                    )
                  } else {
                    Text(
                      text = q.prompt,
                      fontSize = if (q.prompt.length > 20) 22.sp else 32.sp,
                      fontWeight = FontWeight.Black,
                      color = MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Center
                    )
                    if (q.subtitle.isNotEmpty()) {
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                        text = q.subtitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Options
              MultipleChoiceGrid(
                options = q.options,
                selectedIndex = uiState.selectedOptionIndex,
                correctIndex = q.correctIndex,
                onSelectOption = { viewModel.submitAnswer(it) }
              )

              Spacer(modifier = Modifier.height(20.dp))
            }
          }
        }
      }

      // Pause Dialog
      if (uiState.isPaused) {
        AlertDialog(
          onDismissRequest = { viewModel.togglePause() },
          title = { Text("Game Paused") },
          text = { Text("Take a breath! Ready to resume training?") },
          confirmButton = {
            Button(onClick = { viewModel.togglePause() }) {
              Text(stringResource(R.string.action_resume))
            }
          },
          dismissButton = {
            OutlinedButton(onClick = onExit) {
              Text(stringResource(R.string.action_exit))
            }
          }
        )
      }
    }
  }
}
