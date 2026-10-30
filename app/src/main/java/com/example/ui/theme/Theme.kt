package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = IndigoLight,
  onPrimary = Color.White,
  primaryContainer = IndigoDark,
  onPrimaryContainer = Color(0xFFE0E7FF),
  secondary = CyanAccentLight,
  onSecondary = Color(0xFF00363D),
  secondaryContainer = CyanAccentDark,
  onSecondaryContainer = Color(0xFFCFFAFE),
  tertiary = VioletAccentLight,
  onTertiary = Color(0xFF2E1065),
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  error = ErrorRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = IndigoPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0E7FF),
  onPrimaryContainer = Color(0xFF1E1B4B),
  secondary = CyanAccentDark,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFCFFAFE),
  onSecondaryContainer = Color(0xFF164E63),
  tertiary = VioletAccent,
  onTertiary = Color.White,
  background = LightBackground,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightTextSecondary,
  error = ErrorRed,
  onError = Color.White
)

enum class ThemeMode {
  SYSTEM,
  DARK,
  LIGHT
}

@Composable
fun MindForgeTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
  }

  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
