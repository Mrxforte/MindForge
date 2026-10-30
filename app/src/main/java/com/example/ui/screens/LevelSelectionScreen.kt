package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.repository.LevelDisplayItem
import com.example.core.model.GameDefinition
import com.example.ui.theme.GoldXp
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun LevelSelectionScreen(
  game: GameDefinition,
  levels: List<LevelDisplayItem>,
  onBackClick: () -> Unit,
  onSelectLevel: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val completedCount = levels.count { it.isCompleted }
  val totalStars = levels.sumOf { it.stars }

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBackClick) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = game.iconEmoji, fontSize = 20.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = game.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = "${game.category.displayName} • $completedCount/${game.totalLevels} Completed",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GoldXp.copy(alpha = 0.15f)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = GoldXp,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$totalStars",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = GoldXp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Levels Grid (5 columns)
    LazyVerticalGrid(
      columns = GridCells.Fixed(4),
      contentPadding = PaddingValues(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(levels, key = { it.levelNumber }) { lvl ->
        val isUnlocked = lvl.isUnlocked
        val isCompleted = lvl.isCompleted

        Surface(
          shape = RoundedCornerShape(18.dp),
          color = when {
            isCompleted -> MaterialTheme.colorScheme.primaryContainer
            isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
          },
          border = androidx.compose.foundation.BorderStroke(
            width = if (isCompleted || isUnlocked) 1.5.dp else 0.5.dp,
            color = when {
              isCompleted -> MaterialTheme.colorScheme.primary
              isUnlocked -> MaterialTheme.colorScheme.outlineVariant
              else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
          ),
          modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) {
              onSelectLevel(lvl.levelNumber)
            }
        ) {
          Box(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            contentAlignment = Alignment.Center
          ) {
            if (!isUnlocked) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(24.dp)
              )
            } else {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "${lvl.levelNumber}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (isCompleted && lvl.stars > 0) {
                  Row(horizontalArrangement = Arrangement.Center) {
                    repeat(lvl.stars) {
                      Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldXp,
                        modifier = Modifier.size(10.dp)
                      )
                    }
                  }
                } else {
                  Text(
                    text = lvl.config.difficultyTier.title.take(3).uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
