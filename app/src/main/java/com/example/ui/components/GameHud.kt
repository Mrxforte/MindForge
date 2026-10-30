package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldXp
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.WarningOrange

@Composable
fun GameHud(
  levelNumber: Int,
  currentRound: Int,
  totalRounds: Int,
  score: Int,
  combo: Int,
  remainingSeconds: Int,
  totalSeconds: Int,
  onPauseClick: () -> Unit,
  onExitClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val progress = (remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
  val animatedProgress by animateFloatAsState(targetValue = progress, label = "time_progress")
  
  val timerColor by animateColorAsState(
    targetValue = when {
      progress > 0.4f -> CyanAccent
      progress > 0.15f -> WarningOrange
      else -> ErrorRed
    },
    label = "timer_color"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onExitClick,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Exit Game",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          tonalElevation = 2.dp
        ) {
          Text(
            text = "LVL $levelNumber",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      // Center: Score & Combo
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
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
              text = "$score",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        if (combo > 1) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = WarningOrange.copy(alpha = 0.2f)
          ) {
            Text(
              text = "×$combo",
              fontWeight = FontWeight.Black,
              fontSize = 13.sp,
              color = WarningOrange,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Right: Round & Pause button
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "$currentRound/$totalRounds",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = onPauseClick,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Time Progress Indicator
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .weight(1f)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = timerColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "${remainingSeconds}s",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = timerColor
      )
    }
  }
}
