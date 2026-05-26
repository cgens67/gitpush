package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryDark,
  secondary = SecondaryDark,
  tertiary = TertiaryDark,
  background = BackgroundDark,
  surface = SurfaceDark,
  onPrimary = OnPrimaryDark,
  onSecondary = OnSecondaryDark,
  onBackground = OnBackgroundDark,
  onSurface = OnSurfaceDark,
  primaryContainer = Color(0xFF004481),
  onPrimaryContainer = Color(0xFFD6E3FF),
  secondaryContainer = Color(0xFF3B475B),
  onSecondaryContainer = Color(0xFFDAE2F9),
  surfaceVariant = Color(0xFF2C3241),
  onSurfaceVariant = Color(0xFFC3C6CF),
  outline = Color(0xFF8D9199)
)

private val LightColorScheme = lightColorScheme(
  primary = PrimaryLight,
  secondary = SecondaryLight,
  tertiary = TertiaryLight,
  background = BackgroundLight,
  surface = SurfaceLight,
  onPrimary = OnPrimaryLight,
  onSecondary = OnSecondaryLight,
  onBackground = OnBackgroundLight,
  onSurface = OnSurfaceLight,
  primaryContainer = Color(0xFFD6E3FF),
  onPrimaryContainer = Color(0xFF001B4B),
  secondaryContainer = Color(0xFFDAE2F9),
  onSecondaryContainer = Color(0xFF101C31),
  surfaceVariant = Color(0xFFDFE2EB),
  onSurfaceVariant = Color(0xFF43474F),
  outline = Color(0xFF73777F)
)

// Material 3 Expressive guidelines favor playfulness, big curves, and dynamic feels
val ExpressiveShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(20.dp),
  large = RoundedCornerShape(32.dp),
  extraLarge = RoundedCornerShape(40.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Use expressive custom colors by default to avoid plain AI-slop colors
  dynamicColor: Boolean = false, 
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = ExpressiveShapes,
    content = content
  )
}
