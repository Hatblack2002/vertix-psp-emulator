package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VertixGeometricIcon
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

@Composable
fun SplashScreen(
  stepMessage: String,
  onEnterClick: () -> Unit
) {
  val alphaAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    alphaAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = LinearEasing)
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.alpha(alphaAnim.value)
    ) {
      // VERTIX Glowing Icon
      VertixGeometricIcon(sizeDp = 96.dp, showGlow = true)

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "VERTIX",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 6.sp,
        color = VertixTextPrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "PSP EMULATOR",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 3.sp,
        color = VertixSecondary
      )

      Spacer(modifier = Modifier.height(64.dp))

      // Minimalist loading bar
      Box(
        modifier = Modifier
          .width(220.dp)
          .height(3.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(VertixSurfaceSecondary)
      ) {
        LinearProgressIndicator(
          modifier = Modifier
            .fillMaxSize()
            .testTag("splash_progress"),
          color = VertixPrimary,
          trackColor = VertixSurfaceSecondary
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = stepMessage,
        fontSize = 13.sp,
        color = VertixTextSecondary
      )
    }

    // Bottom Slogan
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Juegos. Libertad. Sin límites.",
        fontSize = 12.sp,
        color = VertixTextSecondary.copy(alpha = 0.7f),
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      TextButton(
        onClick = onEnterClick,
        modifier = Modifier.testTag("skip_splash_button")
      ) {
        Text(
          text = "Continuar al inicio →",
          color = VertixSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}
