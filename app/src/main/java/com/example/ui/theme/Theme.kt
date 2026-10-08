package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VertixColorScheme = darkColorScheme(
  primary = VertixPrimary,
  onPrimary = VertixTextPrimary,
  primaryContainer = VertixSurfaceTertiary,
  onPrimaryContainer = VertixTextPrimary,
  secondary = VertixSecondary,
  onSecondary = VertixBg,
  secondaryContainer = VertixSurfaceSecondary,
  onSecondaryContainer = VertixSecondary,
  tertiary = VertixSuccess,
  onTertiary = VertixBg,
  background = VertixBg,
  onBackground = VertixTextPrimary,
  surface = VertixSurface,
  onSurface = VertixTextPrimary,
  surfaceVariant = VertixSurfaceSecondary,
  onSurfaceVariant = VertixTextSecondary,
  outline = VertixBorder,
  outlineVariant = VertixBorderActive,
  error = VertixError,
  onError = VertixBg
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // VERTIX is always dark and cinematic
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = VertixBg.toArgb()
        window.navigationBarColor = VertixBg.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = VertixColorScheme,
    typography = Typography,
    content = content
  )
}
