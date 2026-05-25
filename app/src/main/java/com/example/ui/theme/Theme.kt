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
  onSurface = OnSurfaceDark
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
  primaryContainer = Color(0xFFEADDFF),
  onPrimaryContainer = Color(0xFF21005D),
  secondaryContainer = Color(0xFFE8DEF8),
  onSecondaryContainer = Color(0xFF49454F),
  surfaceVariant = Color(0xFFE7E0EC),
  onSurfaceVariant = Color(0xFF49454F),
  outline = Color(0xFF79747E)
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
