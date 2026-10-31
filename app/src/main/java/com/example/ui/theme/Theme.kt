package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = DarkPrimaryGreen,
  onPrimary = Color.Black,
  primaryContainer = DarkPrimaryContainer,
  onPrimaryContainer = PrimaryGreenContainer,
  secondary = SecondaryTeal,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF004D40),
  onSecondaryContainer = SecondaryTealContainer,
  tertiary = AccentAmber,
  background = DarkBackground,
  onBackground = Color(0xFFE2E3DF),
  surface = DarkSurface,
  onSurface = Color(0xFFE2E3DF),
  surfaceVariant = DarkCard,
  onSurfaceVariant = Color(0xFFC2C8C1)
)

private val LightColorScheme = lightColorScheme(
  primary = PrimaryGreenLight,
  onPrimary = Color.White,
  primaryContainer = PrimaryGreenContainer,
  onPrimaryContainer = OnPrimaryGreenContainer,
  secondary = SecondaryTeal,
  onSecondary = Color.White,
  secondaryContainer = SecondaryTealContainer,
  onSecondaryContainer = Color(0xFF003830),
  tertiary = AccentAmber,
  background = LightBackground,
  onBackground = TextPrimary,
  surface = LightSurface,
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFFF1F5F1),
  onSurfaceVariant = TextSecondary
)

@Composable
fun CatatBisniskuTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
