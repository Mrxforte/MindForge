package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.GameResult
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldXp
import com.example.ui.theme.SuccessGreen

@Composable
fun ResultScreen(
  result: GameResult,
  onNextLevel: () -> Unit,
  onRetry: () -> Unit,
  onExit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val starScale = remember { Animatable(0.2f) }
  LaunchedEffect(Unit) {
    starScale.animateTo(1f, animationSpec = tween(500))
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Header Trophy / Star Icon
    Surface(
      shape = CircleShape,
      color = if (result.isCompleted) GoldXp.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier
        .size(90.dp)
        .scale(starScale.value)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = null,
          tint = if (result.isCompleted) GoldXp else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(48.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = if (result.isCompleted) stringResource(R.string.result_victory) else stringResource(R.string.result_game_over),
      fontSize = 26.sp,
      fontWeight = FontWeight.Black,
      color = MaterialTheme.colorScheme.onBackground
    )

    if (result.isNewHighScore) {
      Spacer(modifier = Modifier.height(6.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = CyanAccent.copy(alpha = 0.2f)
      ) {
        Text(
          text = "🏆 " + stringResource(R.string.result_best_score),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = CyanAccent,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Stars Row
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.scale(starScale.value)
    ) {
      for (i in 1..3) {
        val earned = i <= result.stars
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (earned) GoldXp else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
          modifier = Modifier.size(36.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Stats Grid Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Final Score", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
          Text(text = "${result.score}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Accuracy", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
          Text(text = "${result.accuracy}% (${result.correctAnswers}/${result.totalAnswers})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SuccessGreen)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
          Text(text = "${result.durationMs / 1000}s", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "XP Earned", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
          Text(text = "+${result.xpEarned} XP", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GoldXp)
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Actions
    if (result.isCompleted) {
      Button(
        onClick = onNextLevel,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Text(text = stringResource(R.string.action_next_level), fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = onRetry,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
      ) {
        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = stringResource(R.string.action_retry), fontWeight = FontWeight.SemiBold)
      }

      Button(
        onClick = onExit,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
      ) {
        Text(text = stringResource(R.string.action_back), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}
