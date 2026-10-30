package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.core.model.GameCategory
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CognitiveRadarChart(
  scores: Map<GameCategory, Int>,
  modifier: Modifier = Modifier,
  tintColor: Color = IndigoPrimary
) {
  val animationProgress = remember { Animatable(0f) }
  LaunchedEffect(scores) {
    animationProgress.animateTo(1f, animationSpec = tween(900))
  }

  val categories = listOf(
    GameCategory.MATH,
    GameCategory.MEMORY,
    GameCategory.LOGIC,
    GameCategory.ATTENTION,
    GameCategory.REACTION,
    GameCategory.SPATIAL
  )

  val onSurfaceColor = MaterialTheme.colorScheme.onSurface

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = minOf(size.width, size.height) * 0.38f
      val angleStep = (2 * Math.PI / categories.size).toFloat()

      // Draw 4 concentric webs
      val levels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
      for (lvl in levels) {
        val webRadius = maxRadius * lvl
        val webPath = Path()
        for (i in categories.indices) {
          val angle = i * angleStep - (Math.PI / 2).toFloat()
          val x = center.x + webRadius * cos(angle)
          val y = center.y + webRadius * sin(angle)
          if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
        }
        webPath.close()
        drawPath(
          path = webPath,
          color = tintColor.copy(alpha = 0.15f),
          style = Stroke(width = 1.5.dp.toPx())
        )
      }

      // Draw axis spokes
      for (i in categories.indices) {
        val angle = i * angleStep - (Math.PI / 2).toFloat()
        val x = center.x + maxRadius * cos(angle)
        val y = center.y + maxRadius * sin(angle)
        drawLine(
          color = tintColor.copy(alpha = 0.25f),
          start = center,
          end = Offset(x, y),
          strokeWidth = 1.dp.toPx()
        )

        // Draw category labels
        val labelRadius = maxRadius * 1.18f
        val lx = center.x + labelRadius * cos(angle)
        val ly = center.y + labelRadius * sin(angle)

        drawContext.canvas.nativeCanvas.apply {
          val paint = android.graphics.Paint().apply {
            color = onSurfaceColor.hashCode()
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
          }
          drawText(categories[i].displayName.take(7), lx, ly + 8f, paint)
        }
      }

      // Draw user's polygon
      val polyPath = Path()
      val points = mutableListOf<Offset>()

      for (i in categories.indices) {
        val cat = categories[i]
        val score = (scores[cat] ?: 60).coerceIn(20, 100) / 100f
        val animatedRadius = maxRadius * score * animationProgress.value
        val angle = i * angleStep - (Math.PI / 2).toFloat()
        val px = center.x + animatedRadius * cos(angle)
        val py = center.y + animatedRadius * sin(angle)
        points.add(Offset(px, py))
        if (i == 0) polyPath.moveTo(px, py) else polyPath.lineTo(px, py)
      }
      polyPath.close()

      // Fill area with subtle gradient tint
      drawPath(
        path = polyPath,
        color = CyanAccent.copy(alpha = 0.35f)
      )

      // Stroke outline
      drawPath(
        path = polyPath,
        color = CyanAccent,
        style = Stroke(width = 2.5.dp.toPx())
      )

      // Draw glowing node circles
      for (pt in points) {
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
        drawCircle(color = CyanAccent, radius = 2.dp.toPx(), center = pt)
      }
    }
  }
}
