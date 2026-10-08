package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ControlSettings
import com.example.model.PerformanceMetrics
import com.example.model.PspGame
import com.example.ui.components.VertixPrimaryButton
import com.example.ui.components.VertixSecondaryButton
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary
import kotlin.math.roundToInt

@Composable
fun GameRunningScreen(
  game: PspGame,
  controlSettings: ControlSettings,
  performanceMetrics: PerformanceMetrics,
  isQuickMenuOpen: Boolean,
  onOpenQuickMenu: () -> Unit,
  onCloseQuickMenu: () -> Unit,
  onSaveState: () -> Unit,
  onLoadState: () -> Unit,
  onExitGame: () -> Unit
) {
  val view = LocalView.current

  fun triggerHaptic() {
    if (controlSettings.hapticFeedback) {
      view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    val isLandscape = maxWidth > maxHeight

    // PSP 16:9 Display Canvas
    Box(
      modifier = Modifier
        .fillMaxSize()
        .align(Alignment.Center)
    ) {
      Image(
        painter = painterResource(id = R.drawable.gameplay_gow),
        contentDescription = "Pantalla de renderizado de juego",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
      )

      // Title watermark / Game indicator
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 10.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color.Black.copy(alpha = 0.45f))
          .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = game.title.uppercase(),
          fontFamily = OrbitronFontFamily,
          fontSize = 10.sp,
          color = Color.White.copy(alpha = 0.8f),
          letterSpacing = 1.sp
        )
      }

      // HUD Performance Overlay
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 12.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color.Black.copy(alpha = 0.6f))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "FPS: ${performanceMetrics.fps}",
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (performanceMetrics.fps >= 55f) Color(0xFF35D6A0) else Color(0xFFFFBE63)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${performanceMetrics.speedPercent.toInt()}%",
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          fontSize = 10.sp,
          color = VertixSecondary
        )
      }
    }

    // Virtual Touch Controls Layer
    if (controlSettings.touchEnabled) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .alpha(controlSettings.opacity)
      ) {
        // Shoulder Button L (Top Left)
        VirtualShoulderTrigger(
          label = "L",
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 16.dp, top = 8.dp),
          onTouch = { triggerHaptic() }
        )

        // Shoulder Button R (Top Right)
        VirtualShoulderTrigger(
          label = "R",
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 16.dp, top = 8.dp),
          onTouch = { triggerHaptic() }
        )

        // Left Controls Cluster (D-Pad + Analog Stick)
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          VirtualDPad(
            onDirectionPress = { triggerHaptic() }
          )

          VirtualAnalogStick(
            onMove = { triggerHaptic() }
          )
        }

        // Center Bottom Controls: Quick Menu [▶], [SELECT], [START]
        Row(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Quick Menu Button
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.6f))
              .border(1.dp, VertixPrimary, RoundedCornerShape(8.dp))
              .clickable {
                triggerHaptic()
                onOpenQuickMenu()
              }
              .testTag("in_game_quick_menu_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Pause,
              contentDescription = "Menú Rápido",
              tint = VertixPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          VirtualPillButton(label = "SELECT", onClick = { triggerHaptic() })
          VirtualPillButton(label = "START", onClick = { triggerHaptic() })
        }

        // Right Controls Cluster (Action Buttons: △, ○, ✕, □)
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 32.dp)
        ) {
          VirtualActionButtonsDiamond(
            onTouchButton = { triggerHaptic() }
          )
        }
      }
    }

    // In-Game Quick Menu Modal Overlay (Configuración Rápida)
    AnimatedVisibility(
      visible = isQuickMenuOpen,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.75f))
          .clickable { onCloseQuickMenu() },
        contentAlignment = Alignment.Center
      ) {
        Card(
          modifier = Modifier
            .width(320.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VertixBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = false) {}
            .testTag("quick_menu_dialog"),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Configuración rápida",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = VertixTextPrimary
              )

              IconButton(
                onClick = onCloseQuickMenu,
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Cerrar",
                  tint = VertixTextSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            QuickMenuRow(
              icon = Icons.Default.Save,
              label = "Guardar partida (Ranura 1)",
              onClick = {
                triggerHaptic()
                onSaveState()
              }
            )

            QuickMenuRow(
              icon = Icons.Default.Restore,
              label = "Cargar partida (Ranura 1)",
              onClick = {
                triggerHaptic()
                onLoadState()
              }
            )

            QuickMenuRow(
              icon = Icons.Default.Gamepad,
              label = "Controles táctiles",
              onClick = {
                triggerHaptic()
                onCloseQuickMenu()
              }
            )

            QuickMenuRow(
              icon = Icons.Default.Settings,
              label = "Ajustes del juego",
              onClick = {
                triggerHaptic()
                onCloseQuickMenu()
              }
            )

            QuickMenuRow(
              icon = Icons.Default.Close,
              label = "Salir al menú",
              isDestructive = true,
              onClick = {
                triggerHaptic()
                onExitGame()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun VirtualShoulderTrigger(
  label: String,
  modifier: Modifier = Modifier,
  onTouch: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .width(84.dp)
      .height(34.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(if (isPressed) VertixPrimary.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f))
      .border(
        width = 1.dp,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(interactionSource = interactionSource, indication = null) { onTouch() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = OrbitronFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.9f)
    )
  }
}

@Composable
fun VirtualDPad(
  onDirectionPress: () -> Unit
) {
  Box(
    modifier = Modifier.size(130.dp),
    contentAlignment = Alignment.Center
  ) {
    // Up
    DPadArrowButton(
      direction = "UP",
      modifier = Modifier
        .align(Alignment.TopCenter)
        .size(42.dp),
      onPress = onDirectionPress
    )
    // Down
    DPadArrowButton(
      direction = "DOWN",
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .size(42.dp),
      onPress = onDirectionPress
    )
    // Left
    DPadArrowButton(
      direction = "LEFT",
      modifier = Modifier
        .align(Alignment.CenterStart)
        .size(42.dp),
      onPress = onDirectionPress
    )
    // Right
    DPadArrowButton(
      direction = "RIGHT",
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .size(42.dp),
      onPress = onDirectionPress
    )
    // Center circle
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(Color.Black.copy(alpha = 0.3f))
        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    )
  }
}

@Composable
fun DPadArrowButton(
  direction: String,
  modifier: Modifier = Modifier,
  onPress: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isPressed) VertixPrimary.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.45f))
      .border(
        width = 1.dp,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(interactionSource = interactionSource, indication = null) { onPress() },
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(16.dp)) {
      val w = size.width
      val h = size.height
      val arrowPath = Path().apply {
        when (direction) {
          "UP" -> {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
          }
          "DOWN" -> {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w * 0.5f, h)
            close()
          }
          "LEFT" -> {
            moveTo(0f, h * 0.5f)
            lineTo(w, 0f)
            lineTo(w, h)
            close()
          }
          "RIGHT" -> {
            moveTo(0f, 0f)
            lineTo(w, h * 0.5f)
            lineTo(0f, h)
            close()
          }
        }
      }
      drawPath(
        path = arrowPath,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.75f)
      )
    }
  }
}

@Composable
fun VirtualAnalogStick(
  onMove: () -> Unit
) {
  var stickOffset by remember { mutableStateOf(Offset.Zero) }
  val maxRadius = 36f

  Box(
    modifier = Modifier
      .size(100.dp)
      .clip(CircleShape)
      .background(Color.Black.copy(alpha = 0.35f))
      .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { onMove() },
          onDragEnd = { stickOffset = Offset.Zero },
          onDragCancel = { stickOffset = Offset.Zero },
          onDrag = { change, dragAmount ->
            change.consume()
            val newOffset = stickOffset + dragAmount
            val distance = newOffset.getDistance()
            stickOffset = if (distance > maxRadius) {
              newOffset * (maxRadius / distance)
            } else {
              newOffset
            }
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    // Draggable center thumb
    Box(
      modifier = Modifier
        .offset { IntOffset(stickOffset.x.roundToInt(), stickOffset.y.roundToInt()) }
        .size(46.dp)
        .clip(CircleShape)
        .background(Color(0xFF26384F).copy(alpha = 0.8f))
        .border(1.dp, VertixSecondary.copy(alpha = 0.6f), CircleShape)
    )
  }
}

@Composable
fun VirtualActionButtonsDiamond(
  onTouchButton: () -> Unit
) {
  Box(
    modifier = Modifier.size(150.dp),
    contentAlignment = Alignment.Center
  ) {
    // Triangle △ (Top)
    ActionButton(
      symbol = "△",
      symbolColor = Color(0xFF35D6A0),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .size(44.dp),
      onClick = onTouchButton
    )

    // Circle ○ (Right)
    ActionButton(
      symbol = "○",
      symbolColor = Color(0xFFFF647C),
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .size(44.dp),
      onClick = onTouchButton
    )

    // Cross ✕ (Bottom)
    ActionButton(
      symbol = "✕",
      symbolColor = Color(0xFF00E5FF),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .size(44.dp),
      onClick = onTouchButton
    )

    // Square □ (Left)
    ActionButton(
      symbol = "□",
      symbolColor = Color(0xFFFFBE63),
      modifier = Modifier
        .align(Alignment.CenterStart)
        .size(44.dp),
      onClick = onTouchButton
    )
  }
}

@Composable
fun ActionButton(
  symbol: String,
  symbolColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(if (isPressed) VertixPrimary.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.45f))
      .border(
        width = 1.dp,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.25f),
        shape = CircleShape
      )
      .clickable(interactionSource = interactionSource, indication = null) { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = symbol,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = if (isPressed) Color.White else symbolColor.copy(alpha = 0.9f)
    )
  }
}

@Composable
fun VirtualPillButton(
  label: String,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = Modifier
      .height(26.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(if (isPressed) VertixPrimary.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.45f))
      .border(
        width = 0.5.dp,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
      )
      .clickable(interactionSource = interactionSource, indication = null) { onClick() }
      .padding(horizontal = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = OrbitronFontFamily,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.75f),
      letterSpacing = 1.sp
    )
  }
}

@Composable
fun QuickMenuRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isDestructive: Boolean = false,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (isDestructive) Color(0xFFFF647C) else VertixSecondary,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = label,
      fontSize = 13.sp,
      color = if (isDestructive) Color(0xFFFF647C) else VertixTextPrimary,
      fontWeight = FontWeight.Medium
    )
  }
}
