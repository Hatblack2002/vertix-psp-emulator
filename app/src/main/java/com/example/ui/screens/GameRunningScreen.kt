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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.engine.VertixPspButtons
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

/**
 * Callbacks de entrada táctil hacia el núcleo nativo.
 * - `onButtonInput(buttonMask, isPressed)`: botones digitales (D-Pad, △○✕□, L/R, START/SELECT).
 * - `onAnalogInput(x, y)`: stick analógico normalizado [-1, 1] en cada eje.
 *
 * Implementación recomendada: `viewModel::onButtonInput` y `viewModel::onAnalogInput`,
 * que a su vez invocan `engineService.setButtonState` / `setAnalogStick`, los cuales
 * terminan llamando a `NativeVertixCore.nativeSendInput(buttonMask, analogX, analogY)`.
 */
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
  onExitGame: () -> Unit,
  onButtonInput: (Int, Boolean) -> Unit = { _, _ -> },
  onAnalogInput: (Float, Float) -> Unit = { _, _ -> }
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
          buttonMask = VertixPspButtons.L_TRIGGER,
          onButtonInput = { mask, pressed ->
            triggerHaptic()
            onButtonInput(mask, pressed)
          },
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 16.dp, top = 8.dp)
        )

        // Shoulder Button R (Top Right)
        VirtualShoulderTrigger(
          label = "R",
          buttonMask = VertixPspButtons.R_TRIGGER,
          onButtonInput = { mask, pressed ->
            triggerHaptic()
            onButtonInput(mask, pressed)
          },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 16.dp, top = 8.dp)
        )

        // Left Controls Cluster (D-Pad + Analog Stick)
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          VirtualDPad(
            onDirectionChange = { direction, isPressed ->
              triggerHaptic()
              val mask = when (direction) {
                "UP" -> VertixPspButtons.DPAD_UP
                "DOWN" -> VertixPspButtons.DPAD_DOWN
                "LEFT" -> VertixPspButtons.DPAD_LEFT
                "RIGHT" -> VertixPspButtons.DPAD_RIGHT
                else -> return@VirtualDPad
              }
              onButtonInput(mask, isPressed)
            }
          )

          VirtualAnalogStick(
            onStickMove = { x, y ->
              onAnalogInput(x, y)
            },
            onStickRelease = {
              onAnalogInput(0f, 0f)
            }
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

          VirtualPillButton(label = "SELECT", onClick = {
            triggerHaptic()
            // SELECT es un pulso: emitimos press + release inmediatos.
            onButtonInput(VertixPspButtons.SELECT, true)
            onButtonInput(VertixPspButtons.SELECT, false)
          })
          VirtualPillButton(label = "START", onClick = {
            triggerHaptic()
            onButtonInput(VertixPspButtons.START, true)
            onButtonInput(VertixPspButtons.START, false)
          })
        }

        // Right Controls Cluster (Action Buttons: △, ○, ✕, □)
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 32.dp)
        ) {
          VirtualActionButtonsDiamond(
            onButtonTouch = { symbol, isPressed ->
              triggerHaptic()
              val mask = when (symbol) {
                "△" -> VertixPspButtons.TRIANGLE
                "○" -> VertixPspButtons.CIRCLE
                "✕" -> VertixPspButtons.CROSS
                "□" -> VertixPspButtons.SQUARE
                else -> return@VirtualActionButtonsDiamond
              }
              onButtonInput(mask, isPressed)
            }
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
  buttonMask: Int,
  onButtonInput: (Int, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Emite el evento press/release al núcleo nativo en cada transición.
  LaunchedEffect(isPressed) {
    onButtonInput(buttonMask, isPressed)
  }

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
      .clickable(interactionSource = interactionSource, indication = null) {
        // El LaunchedEffect(isPressed) se encarga de emitir los eventos reales;
        // este clickable solo absorbe el tap para que el InteractionSource cambie.
      },
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
  onDirectionChange: (String, Boolean) -> Unit
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
      onPressChange = { isPressed -> onDirectionChange("UP", isPressed) }
    )
    // Down
    DPadArrowButton(
      direction = "DOWN",
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .size(42.dp),
      onPressChange = { isPressed -> onDirectionChange("DOWN", isPressed) }
    )
    // Left
    DPadArrowButton(
      direction = "LEFT",
      modifier = Modifier
        .align(Alignment.CenterStart)
        .size(42.dp),
      onPressChange = { isPressed -> onDirectionChange("LEFT", isPressed) }
    )
    // Right
    DPadArrowButton(
      direction = "RIGHT",
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .size(42.dp),
      onPressChange = { isPressed -> onDirectionChange("RIGHT", isPressed) }
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
  onPressChange: (Boolean) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Emite press/release al núcleo nativo.
  LaunchedEffect(isPressed) {
    onPressChange(isPressed)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isPressed) VertixPrimary.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.45f))
      .border(
        width = 1.dp,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(interactionSource = interactionSource, indication = null) {
        // El LaunchedEffect(isPressed) emite los eventos reales.
      },
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
  onStickMove: (Float, Float) -> Unit,
  onStickRelease: () -> Unit
) {
  var stickOffset by remember { mutableStateOf(Offset.Zero) }
  val maxRadius = 36f

  // Normaliza el offset a [-1, +1] y emite al núcleo nativo.
  LaunchedEffect(stickOffset) {
    if (stickOffset == Offset.Zero) {
      onStickRelease()
    } else {
      val nx = stickOffset.x / maxRadius
      val ny = stickOffset.y / maxRadius
      onStickMove(nx.coerceIn(-1f, 1f), ny.coerceIn(-1f, 1f))
    }
  }

  Box(
    modifier = Modifier
      .size(100.dp)
      .clip(CircleShape)
      .background(Color.Black.copy(alpha = 0.35f))
      .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { /* LaunchedEffect(stickOffset) emitirá el primer valor. */ },
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
  onButtonTouch: (String, Boolean) -> Unit
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
      onPressChange = { isPressed -> onButtonTouch("△", isPressed) }
    )

    // Circle ○ (Right)
    ActionButton(
      symbol = "○",
      symbolColor = Color(0xFFFF647C),
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .size(44.dp),
      onPressChange = { isPressed -> onButtonTouch("○", isPressed) }
    )

    // Cross ✕ (Bottom)
    ActionButton(
      symbol = "✕",
      symbolColor = Color(0xFF00E5FF),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .size(44.dp),
      onPressChange = { isPressed -> onButtonTouch("✕", isPressed) }
    )

    // Square □ (Left)
    ActionButton(
      symbol = "□",
      symbolColor = Color(0xFFFFBE63),
      modifier = Modifier
        .align(Alignment.CenterStart)
        .size(44.dp),
      onPressChange = { isPressed -> onButtonTouch("□", isPressed) }
    )
  }
}

@Composable
fun ActionButton(
  symbol: String,
  symbolColor: Color,
  modifier: Modifier = Modifier,
  onPressChange: (Boolean) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Emite press/release al núcleo nativo.
  LaunchedEffect(isPressed) {
    onPressChange(isPressed)
  }

  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(if (isPressed) VertixPrimary.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.45f))
      .border(
        width = 1.dp,
        color = if (isPressed) VertixSecondary else Color.White.copy(alpha = 0.25f),
        shape = CircleShape
      )
      .clickable(interactionSource = interactionSource, indication = null) {
        // El LaunchedEffect(isPressed) emite los eventos reales.
      },
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
