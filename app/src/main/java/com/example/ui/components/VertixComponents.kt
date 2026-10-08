package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompatibilityStatus
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixCardBg
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextDisabled
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

@Composable
fun VertixGeometricIcon(
  modifier: Modifier = Modifier,
  sizeDp: Dp = 48.dp,
  showGlow: Boolean = true
) {
  Box(
    modifier = modifier.size(sizeDp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(sizeDp)) {
      val w = size.width
      val h = size.height

      // Left wing (Purple/Violet gradient)
      val leftWing = Path().apply {
        moveTo(w * 0.16f, h * 0.22f)
        lineTo(w * 0.42f, h * 0.44f)
        lineTo(w * 0.50f, h * 0.88f)
        lineTo(w * 0.38f, h * 0.56f)
        lineTo(w * 0.22f, h * 0.48f)
        close()
      }

      val leftWingOuter = Path().apply {
        moveTo(w * 0.20f, h * 0.38f)
        lineTo(w * 0.36f, h * 0.52f)
        lineTo(w * 0.30f, h * 0.64f)
        lineTo(w * 0.16f, h * 0.48f)
        close()
      }

      // Right wing (Cyan gradient)
      val rightWing = Path().apply {
        moveTo(w * 0.84f, h * 0.22f)
        lineTo(w * 0.58f, h * 0.44f)
        lineTo(w * 0.50f, h * 0.88f)
        lineTo(w * 0.62f, h * 0.56f)
        lineTo(w * 0.78f, h * 0.48f)
        close()
      }

      val rightWingOuter = Path().apply {
        moveTo(w * 0.80f, h * 0.38f)
        lineTo(w * 0.64f, h * 0.52f)
        lineTo(w * 0.70f, h * 0.64f)
        lineTo(w * 0.84f, h * 0.48f)
        close()
      }

      // Glow behind
      if (showGlow) {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0x407B3DFF), Color(0x3000E5FF), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.6f
          )
        )
      }

      // Draw left shapes
      drawPath(
        path = leftWing,
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFF9E6BFF), Color(0xFF7B3DFF), Color(0xFF4C1D95)),
          start = Offset(w * 0.16f, h * 0.22f),
          end = Offset(w * 0.50f, h * 0.88f)
        )
      )
      drawPath(
        path = leftWingOuter,
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFFB794F6), Color(0xFF7B3DFF)),
          start = Offset(w * 0.2f, h * 0.38f),
          end = Offset(w * 0.36f, h * 0.52f)
        )
      )

      // Draw right shapes
      drawPath(
        path = rightWing,
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFF38BDF8), Color(0xFF00E5FF), Color(0xFF0284C7)),
          start = Offset(w * 0.84f, h * 0.22f),
          end = Offset(w * 0.50f, h * 0.88f)
        )
      )
      drawPath(
        path = rightWingOuter,
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFF7DD3FC), Color(0xFF00E5FF)),
          start = Offset(w * 0.8f, h * 0.38f),
          end = Offset(w * 0.64f, h * 0.52f)
        )
      )
    }
  }
}

@Composable
fun VertixBrandLogo(
  modifier: Modifier = Modifier,
  iconSize: Dp = 32.dp,
  showDescriptor: Boolean = true,
  horizontal: Boolean = true
) {
  if (horizontal) {
    Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically
    ) {
      VertixGeometricIcon(sizeDp = iconSize)
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "VERTIX",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          letterSpacing = 2.sp,
          color = VertixTextPrimary
        )
        if (showDescriptor) {
          Text(
            text = "PSP EMULATOR",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = VertixSecondary
          )
        }
      }
    }
  } else {
    Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      VertixGeometricIcon(sizeDp = iconSize)
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "VERTIX",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 4.sp,
        color = VertixTextPrimary
      )
      if (showDescriptor) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "PSP EMULATOR",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp,
          letterSpacing = 2.sp,
          color = VertixSecondary
        )
      }
    }
  }
}

@Composable
fun CompatibilityBadge(
  status: CompatibilityStatus,
  modifier: Modifier = Modifier
) {
  val badgeColor = Color(status.colorHex)
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(badgeColor.copy(alpha = 0.12f))
      .border(0.5.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
      .padding(horizontal = 7.dp, vertical = 3.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(badgeColor)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = status.label.replace("● ", ""),
        color = badgeColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun VertixPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  testTag: String = "primary_button",
  enabled: Boolean = true
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .testTag(testTag),
    enabled = enabled,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = VertixPrimary,
      contentColor = VertixTextPrimary,
      disabledContainerColor = VertixSurfaceTertiary,
      disabledContentColor = VertixTextDisabled
    )
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp
    )
  }
}

@Composable
fun VertixSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  testTag: String = "secondary_button",
  enabled: Boolean = true
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .testTag(testTag),
    enabled = enabled,
    shape = RoundedCornerShape(12.dp),
    border = ButtonDefaults.outlinedButtonBorder.copy(
      brush = Brush.linearGradient(listOf(VertixBorder, VertixBorder))
    ),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = VertixSurfaceSecondary,
      contentColor = VertixTextPrimary
    )
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = VertixTextSecondary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp
    )
  }
}

@Composable
fun FilterChipTab(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "filter_chip"
) {
  Box(
    modifier = modifier
      .testTag(testTag)
      .clip(RoundedCornerShape(10.dp))
      .background(
        if (selected) VertixSurfaceTertiary else VertixSurfaceSecondary.copy(alpha = 0.5f)
      )
      .border(
        width = 1.dp,
        color = if (selected) VertixPrimary else VertixBorder.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    Text(
      text = text,
      color = if (selected) VertixTextPrimary else VertixTextSecondary,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
      fontSize = 13.sp
    )
  }
}

@Composable
fun EmptyLibraryState(
  onAddGamesClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Abstract geometric illustration
    Box(
      modifier = Modifier
        .size(110.dp)
        .clip(CircleShape)
        .background(VertixSurfaceSecondary)
        .border(1.dp, VertixBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      VertixGeometricIcon(sizeDp = 56.dp, showGlow = false)
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Tu biblioteca está vacía",
      fontFamily = OrbitronFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      color = VertixTextPrimary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Selecciona una carpeta o importa archivos ISO y CSO de PSP para comenzar a jugar.",
      fontSize = 13.sp,
      color = VertixTextSecondary,
      textAlign = TextAlign.Center,
      lineHeight = 18.sp,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    VertixPrimaryButton(
      text = "Añadir juegos",
      onClick = onAddGamesClick,
      testTag = "empty_state_add_games"
    )
  }
}

@Composable
fun TechnicalErrorDialog(
  title: String,
  message: String,
  technicalDetails: String? = null,
  onDismiss: () -> Unit,
  onPrimaryAction: (() -> Unit)? = null,
  primaryActionLabel: String = "Cerrar"
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    containerColor = VertixSurface,
    tonalElevation = 6.dp,
    title = {
      Text(
        text = title,
        fontFamily = OrbitronFontFamily,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = VertixTextPrimary
      )
    },
    text = {
      Column {
        Text(
          text = message,
          fontSize = 13.sp,
          color = VertixTextSecondary,
          lineHeight = 18.sp
        )
        if (technicalDetails != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(VertixBg)
              .border(0.5.dp, VertixBorder, RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Text(
              text = technicalDetails,
              fontSize = 11.sp,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              color = VertixSecondary
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onPrimaryAction?.invoke()
          onDismiss()
        }
      ) {
        Text(
          text = primaryActionLabel,
          color = VertixPrimary,
          fontWeight = FontWeight.Bold
        )
      }
    }
  )
}
