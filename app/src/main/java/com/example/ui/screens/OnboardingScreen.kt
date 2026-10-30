package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen

data class OnboardingStep(
  val icon: ImageVector,
  val iconColor: Color,
  val title: String,
  val desc: String
)

@Composable
fun OnboardingScreen(
  onFinishOnboarding: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentStep by remember { mutableIntStateOf(0) }

  val steps = listOf(
    OnboardingStep(
      icon = Icons.Default.Psychology,
      iconColor = IndigoPrimary,
      title = stringResource(R.string.onboarding_title_1),
      desc = stringResource(R.string.onboarding_desc_1)
    ),
    OnboardingStep(
      icon = Icons.Default.Bolt,
      iconColor = CyanAccent,
      title = stringResource(R.string.onboarding_title_2),
      desc = stringResource(R.string.onboarding_desc_2)
    ),
    OnboardingStep(
      icon = Icons.Default.Shield,
      iconColor = SuccessGreen,
      title = stringResource(R.string.onboarding_title_3),
      desc = stringResource(R.string.onboarding_desc_3)
    )
  )

  val active = steps[currentStep]

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Skip Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      TextButton(onClick = onFinishOnboarding) {
        Text(
          text = stringResource(R.string.onboarding_skip),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Center Content
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(horizontal = 16.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = active.iconColor.copy(alpha = 0.15f),
        modifier = Modifier.size(120.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = active.icon,
            contentDescription = null,
            tint = active.iconColor,
            modifier = Modifier.size(64.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      Text(
        text = active.title,
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = active.desc,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Dots Indicator
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.indices.forEach { index ->
          Box(
            modifier = Modifier
              .size(if (index == currentStep) 24.dp else 8.dp, 8.dp)
              .clip(CircleShape)
              .background(
                if (index == currentStep) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
              )
          )
        }
      }
    }

    // Bottom Navigation Button
    Column(modifier = Modifier.fillMaxWidth()) {
      Button(
        onClick = {
          if (currentStep < steps.size - 1) {
            currentStep++
          } else {
            onFinishOnboarding()
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
      ) {
        Text(
          text = if (currentStep < steps.size - 1) stringResource(R.string.action_continue) else stringResource(R.string.onboarding_get_started),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
