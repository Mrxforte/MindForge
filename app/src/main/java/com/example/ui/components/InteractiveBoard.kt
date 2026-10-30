package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.gameengine.GridMemoryPuzzle
import com.example.core.gameengine.MemoryCard
import com.example.core.gameengine.SudokuCell
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.LogicColor
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

// 1. Multiple Choice Grid
@Composable
fun MultipleChoiceGrid(
  options: List<String>,
  selectedIndex: Int?,
  correctIndex: Int?,
  onSelectOption: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val columns = if (options.size <= 2) 1 else 2
  
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    options.chunked(columns).forEachIndexed { rowIndex, rowOpts ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        rowOpts.forEachIndexed { colIndex, text ->
          val optIndex = rowIndex * columns + colIndex
          val isSelected = selectedIndex == optIndex
          val isCorrect = correctIndex == optIndex

          val backgroundColor by animateColorAsState(
            targetValue = when {
              selectedIndex != null && isCorrect -> SuccessGreen.copy(alpha = 0.25f)
              selectedIndex != null && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.25f)
              isSelected -> MaterialTheme.colorScheme.primaryContainer
              else -> MaterialTheme.colorScheme.surface
            },
            label = "option_bg"
          )

          val borderColor by animateColorAsState(
            targetValue = when {
              selectedIndex != null && isCorrect -> SuccessGreen
              selectedIndex != null && isSelected && !isCorrect -> ErrorRed
              isSelected -> MaterialTheme.colorScheme.primary
              else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
            label = "option_border"
          )

          Surface(
            modifier = Modifier
              .weight(1f)
              .height(if (options.size <= 2) 64.dp else 56.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable(enabled = selectedIndex == null) {
                onSelectOption(optIndex)
              },
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
            tonalElevation = 2.dp
          ) {
            Box(
              modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = text,
                fontSize = if (text.length > 15) 15.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                  selectedIndex != null && isCorrect -> SuccessGreen
                  selectedIndex != null && isSelected && !isCorrect -> ErrorRed
                  else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}

// 2. Memory Match Card Grid
@Composable
fun MemoryMatchBoard(
  cards: List<MemoryCard>,
  onCardClick: (MemoryCard) -> Unit,
  modifier: Modifier = Modifier
) {
  val columns = if (cards.size <= 8) 4 else 4

  LazyVerticalGrid(
    columns = GridCells.Fixed(columns),
    modifier = modifier.fillMaxWidth().padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(cards, key = { it.id }) { card ->
      val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(300),
        label = "card_flip"
      )

      Box(
        modifier = Modifier
          .aspectRatio(1f)
          .graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
          }
          .clip(RoundedCornerShape(14.dp))
          .background(
            if (card.isMatched) SuccessGreen.copy(alpha = 0.2f)
            else if (rotation > 90f) MaterialTheme.colorScheme.surfaceVariant
            else IndigoPrimary
          )
          .border(
            width = if (card.isMatched) 2.dp else 1.dp,
            color = if (card.isMatched) SuccessGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(14.dp)
          )
          .clickable(enabled = !card.isFaceUp && !card.isMatched) {
            onCardClick(card)
          },
        contentAlignment = Alignment.Center
      ) {
        if (rotation > 90f) {
          Text(
            text = card.symbol,
            fontSize = 32.sp,
            modifier = Modifier.graphicsLayer { rotationY = 180f }
          )
        } else {
          Text(
            text = "⚡",
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.5f)
          )
        }
      }
    }
  }
}

// 3. Visual Grid Memory Board
@Composable
fun VisualGridBoard(
  gridSize: Int,
  isMemorizingPhase: Boolean,
  highlightedIndices: Set<Int>,
  selectedIndices: Set<Int>,
  onCellClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    for (r in 0 until gridSize) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        for (c in 0 until gridSize) {
          val idx = r * gridSize + c
          val isHighlighted = idx in highlightedIndices
          val isSelected = idx in selectedIndices

          val cellColor = when {
            isMemorizingPhase && isHighlighted -> CyanAccent
            !isMemorizingPhase && isSelected && isHighlighted -> SuccessGreen
            !isMemorizingPhase && isSelected && !isHighlighted -> ErrorRed
            else -> MaterialTheme.colorScheme.surfaceVariant
          }

          Box(
            modifier = Modifier
              .size(if (gridSize == 3) 76.dp else 60.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(cellColor)
              .border(
                1.5.dp,
                if (isHighlighted && isMemorizingPhase) CyanAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
              )
              .clickable(enabled = !isMemorizingPhase) {
                onCellClick(idx)
              }
          )
        }
      }
    }
  }
}

// 4. Sequence Recall Board (Neon 4-pad)
@Composable
fun SequenceRecallBoard(
  activePadIndex: Int?,
  onPadClick: (Int) -> Unit,
  isInputEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  val padColors = listOf(
    CyanAccent,
    SuccessGreen,
    WarningOrange,
    IndigoPrimary
  )

  val padLabels = listOf("Cyan", "Emerald", "Amber", "Indigo")

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    itemsIndexed(padColors) { index, color ->
      val isActive = activePadIndex == index
      val scale by animateFloatAsState(targetValue = if (isActive) 1.08f else 1.0f, label = "pad_scale")

      Box(
        modifier = Modifier
          .aspectRatio(1f)
          .scale(scale)
          .clip(RoundedCornerShape(24.dp))
          .background(if (isActive) color else color.copy(alpha = 0.35f))
          .border(
            width = if (isActive) 4.dp else 2.dp,
            color = if (isActive) Color.White else color,
            shape = RoundedCornerShape(24.dp)
          )
          .clickable(enabled = isInputEnabled) {
            onPadClick(index)
          },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = padLabels[index],
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}

// 5. Sudoku Mini 4x4 Board
@Composable
fun SudokuMiniBoard(
  cells: List<SudokuCell>,
  selectedCell: Pair<Int, Int>?,
  onCellSelect: (Int, Int) -> Unit,
  onNumberInput: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
      modifier = Modifier.padding(bottom = 20.dp)
    ) {
      Column(modifier = Modifier.padding(6.dp)) {
        for (r in 0..3) {
          Row {
            for (c in 0..3) {
              val cell = cells.find { it.row == r && it.col == c } ?: SudokuCell(r, c, 0, false, 0)
              val isSelected = selectedCell?.first == r && selectedCell?.second == c

              Box(
                modifier = Modifier
                  .size(64.dp)
                  .padding(2.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    when {
                      isSelected -> MaterialTheme.colorScheme.primaryContainer
                      cell.isGiven -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                      else -> MaterialTheme.colorScheme.surface
                    }
                  )
                  .border(
                    width = if (isSelected) 2.dp else 0.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable {
                    onCellSelect(r, c)
                  },
                contentAlignment = Alignment.Center
              ) {
                if (cell.value > 0) {
                  Text(
                    text = cell.value.toString(),
                    fontSize = 24.sp,
                    fontWeight = if (cell.isGiven) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (cell.isGiven) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }
    }

    // Number Keypad (1, 2, 3, 4, and Clear)
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
      (1..4).forEach { num ->
        Button(
          onClick = { onNumberInput(num) },
          modifier = Modifier.weight(1f).height(50.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(text = "$num", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
      }
      Button(
        onClick = { onNumberInput(0) },
        modifier = Modifier.weight(1f).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(text = "⌫", fontSize = 18.sp, color = MaterialTheme.colorScheme.onErrorContainer)
      }
    }
  }
}
