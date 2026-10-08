package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSettings
import com.example.model.ControlSettings
import com.example.model.GraphicSettings
import com.example.ui.components.FilterChipTab
import com.example.ui.components.VertixSecondaryButton
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

@Composable
fun SettingsScreen(
  graphicSettings: GraphicSettings,
  audioSettings: AudioSettings,
  controlSettings: ControlSettings,
  onUpdateGraphicSettings: (GraphicSettings) -> Unit,
  onUpdateAudioSettings: (AudioSettings) -> Unit,
  onUpdateControlSettings: (ControlSettings) -> Unit,
  onResetSettings: () -> Unit,
  onBackClick: () -> Unit
) {
  var selectedSection by remember { mutableStateOf("Gráficos") }

  val sections = listOf("Gráficos", "Audio", "Controles", "Sistema", "Acerca de")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBackClick) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Volver",
          tint = VertixTextPrimary
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Ajustes",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = VertixTextPrimary
      )
    }

    // Horizontal Section Tabs
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
      items(sections) { section ->
        FilterChipTab(
          text = section,
          selected = selectedSection == section,
          onClick = { selectedSection = section }
        )
      }
    }

    // Settings Content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      when (selectedSection) {
        "Gráficos" -> {
          item {
            SettingsCategoryHeader(title = "Renderizado", subtitle = "Motor de gráficos y salida visual")
          }

          item {
            SettingsSelectorRow(
              title = "Resolución de renderizado",
              currentValue = graphicSettings.renderResolution,
              onPrevious = {
                val next = when (graphicSettings.renderResolution) {
                  "4x (4K)" -> "3x (1440p)"
                  "3x (1440p)" -> "2x (1080p)"
                  "2x (1080p)" -> "1x (480x272)"
                  else -> "4x (4K)"
                }
                onUpdateGraphicSettings(graphicSettings.copy(renderResolution = next))
              },
              onNext = {
                val next = when (graphicSettings.renderResolution) {
                  "1x (480x272)" -> "2x (1080p)"
                  "2x (1080p)" -> "3x (1440p)"
                  "3x (1440p)" -> "4x (4K)"
                  else -> "1x (480x272)"
                }
                onUpdateGraphicSettings(graphicSettings.copy(renderResolution = next))
              }
            )
          }

          item {
            SettingsSelectorRow(
              title = "Modo de renderizado",
              currentValue = graphicSettings.renderingBackend,
              onPrevious = {
                val next = if (graphicSettings.renderingBackend.contains("Vulkan")) "OpenGL ES 3.2" else "Vulkan (recomendado)"
                onUpdateGraphicSettings(graphicSettings.copy(renderingBackend = next))
              },
              onNext = {
                val next = if (graphicSettings.renderingBackend.contains("Vulkan")) "OpenGL ES 3.2" else "Vulkan (recomendado)"
                onUpdateGraphicSettings(graphicSettings.copy(renderingBackend = next))
              }
            )
          }

          item {
            SettingsToggleRow(
              title = "Filtrado de texturas",
              description = "Mejora la nitidez en superficies oblicuas",
              checked = graphicSettings.textureFiltering,
              onCheckedChange = { onUpdateGraphicSettings(graphicSettings.copy(textureFiltering = it)) }
            )
          }

          item {
            SettingsToggleRow(
              title = "Mejora de bordes (antialiasing)",
              description = "Suaviza dientes de sierra en polígonos 3D",
              checked = graphicSettings.antialiasing,
              onCheckedChange = { onUpdateGraphicSettings(graphicSettings.copy(antialiasing = it)) }
            )
          }

          item {
            SettingsToggleRow(
              title = "Postprocesado",
              description = "Filtros de color cinemáticos y scanlines",
              checked = graphicSettings.postProcessing,
              onCheckedChange = { onUpdateGraphicSettings(graphicSettings.copy(postProcessing = it)) }
            )
          }

          item {
            SettingsSelectorRow(
              title = "Límite de FPS",
              currentValue = graphicSettings.fpsLimit,
              onPrevious = {
                val next = if (graphicSettings.fpsLimit == "60 FPS") "30 FPS" else "60 FPS"
                onUpdateGraphicSettings(graphicSettings.copy(fpsLimit = next))
              },
              onNext = {
                val next = if (graphicSettings.fpsLimit == "60 FPS") "Sin límite" else "60 FPS"
                onUpdateGraphicSettings(graphicSettings.copy(fpsLimit = next))
              }
            )
          }
        }

        "Audio" -> {
          item {
            SettingsCategoryHeader(title = "Subsistema de Audio", subtitle = "Sincronización y volumen")
          }

          item {
            SettingsToggleRow(
              title = "Activar audio",
              description = "Emulación del DSP de sonido de PSP",
              checked = audioSettings.enabled,
              onCheckedChange = { onUpdateAudioSettings(audioSettings.copy(enabled = it)) }
            )
          }

          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp)),
              colors = CardDefaults.cardColors(containerColor = VertixSurface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "Volumen del emulador", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VertixTextPrimary)
                  Text(text = "${(audioSettings.volume * 100).toInt()}%", fontSize = 12.sp, color = VertixSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                  value = audioSettings.volume,
                  onValueChange = { onUpdateAudioSettings(audioSettings.copy(volume = it)) },
                  colors = SliderDefaults.colors(
                    thumbColor = VertixPrimary,
                    activeTrackColor = VertixPrimary,
                    inactiveTrackColor = VertixSurfaceSecondary
                  )
                )
              }
            }
          }

          item {
            SettingsSelectorRow(
              title = "Latencia del buffer",
              currentValue = audioSettings.latency,
              onPrevious = {
                val next = if (audioSettings.latency.contains("Baja")) "Media (24ms)" else "Baja (12ms)"
                onUpdateAudioSettings(audioSettings.copy(latency = next))
              },
              onNext = {
                val next = if (audioSettings.latency.contains("Baja")) "Media (24ms)" else "Baja (12ms)"
                onUpdateAudioSettings(audioSettings.copy(latency = next))
              }
            )
          }

          item {
            SettingsToggleRow(
              title = "Sincronización de audio",
              description = "Evita microcortes adaptando la velocidad del flujo",
              checked = audioSettings.audioSync,
              onCheckedChange = { onUpdateAudioSettings(audioSettings.copy(audioSync = it)) }
            )
          }
        }

        "Controles" -> {
          item {
            SettingsCategoryHeader(title = "Controles Táctiles y Físicos", subtitle = "Sensibilidad, opacidad y distribución")
          }

          item {
            SettingsToggleRow(
              title = "Controles en pantalla",
              description = "Mostrar D-Pad, stick y botones sobre el juego",
              checked = controlSettings.touchEnabled,
              onCheckedChange = { onUpdateControlSettings(controlSettings.copy(touchEnabled = it)) }
            )
          }

          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp)),
              colors = CardDefaults.cardColors(containerColor = VertixSurface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "Opacidad de botones", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VertixTextPrimary)
                  Text(text = "${(controlSettings.opacity * 100).toInt()}%", fontSize = 12.sp, color = VertixSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                  value = controlSettings.opacity,
                  onValueChange = { onUpdateControlSettings(controlSettings.copy(opacity = it)) },
                  valueRange = 0.15f..1.0f,
                  colors = SliderDefaults.colors(
                    thumbColor = VertixSecondary,
                    activeTrackColor = VertixSecondary,
                    inactiveTrackColor = VertixSurfaceSecondary
                  )
                )
              }
            }
          }

          item {
            SettingsToggleRow(
              title = "Vibración háptica",
              description = "Respuesta física al pulsar botones táctiles",
              checked = controlSettings.hapticFeedback,
              onCheckedChange = { onUpdateControlSettings(controlSettings.copy(hapticFeedback = it)) }
            )
          }

          item {
            SettingsToggleRow(
              title = "Ocultar al conectar mando físico",
              description = "Detecta automáticamente mandos Bluetooth y USB",
              checked = controlSettings.hideOnGamepad,
              onCheckedChange = { onUpdateControlSettings(controlSettings.copy(hideOnGamepad = it)) }
            )
          }
        }

        "Sistema" -> {
          item {
            SettingsCategoryHeader(title = "Sistema y Almacenamiento", subtitle = "Rutas de archivos y compilación")
          }

          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp)),
              colors = CardDefaults.cardColors(containerColor = VertixSurface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Ruta de biblioteca PSP", fontSize = 11.sp, color = VertixTextSecondary)
                Text(
                  text = "/storage/emulated/0/PSP/ISO",
                  fontSize = 12.sp,
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  color = VertixSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Carpeta de Memory Stick (Partidas)", fontSize = 11.sp, color = VertixTextSecondary)
                Text(
                  text = "/storage/emulated/0/PSP/SAVEDATA",
                  fontSize = 12.sp,
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  color = VertixSecondary
                )
              }
            }
          }

          item {
            VertixSecondaryButton(
              text = "Restablecer ajustes predeterminados",
              icon = Icons.Default.Refresh,
              onClick = onResetSettings,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        "Acerca de" -> {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, VertixBorder, RoundedCornerShape(14.dp)),
              colors = CardDefaults.cardColors(containerColor = VertixSurface)
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Text(
                  text = "VERTIX",
                  fontFamily = OrbitronFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp,
                  color = VertixTextPrimary,
                  letterSpacing = 2.sp
                )
                Text(
                  text = "PSP EMULATOR FOR ANDROID",
                  fontFamily = OrbitronFontFamily,
                  fontSize = 10.sp,
                  color = VertixSecondary,
                  letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Versión: v1.0.4-release", fontSize = 12.sp, color = VertixTextSecondary)
                Text(text = "Compilación: GitHub Actions (Build #342)", fontSize = 12.sp, color = VertixTextSecondary)
                Text(text = "Núcleo de emulación: VERTIX-ARM64-JIT", fontSize = 12.sp, color = VertixTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                  text = "Un proyecto independiente de alto rendimiento creado por y para jugadores de PSP.",
                  fontSize = 12.sp,
                  color = VertixTextSecondary,
                  lineHeight = 17.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingsCategoryHeader(title: String, subtitle: String) {
  Column(modifier = Modifier.padding(vertical = 6.dp)) {
    Text(
      text = title,
      fontFamily = OrbitronFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = VertixTextPrimary
    )
    Text(
      text = subtitle,
      fontSize = 11.sp,
      color = VertixTextSecondary
    )
  }
}

@Composable
fun SettingsToggleRow(
  title: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = VertixSurface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VertixTextPrimary)
        Text(text = description, fontSize = 11.sp, color = VertixTextSecondary)
      }
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = VertixBg,
          checkedTrackColor = VertixSecondary,
          uncheckedThumbColor = VertixTextSecondary,
          uncheckedTrackColor = VertixSurfaceSecondary
        )
      )
    }
  }
}

@Composable
fun SettingsSelectorRow(
  title: String,
  currentValue: String,
  onPrevious: () -> Unit,
  onNext: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = VertixSurface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = VertixTextPrimary,
        modifier = Modifier.weight(1f)
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp)) {
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior", tint = VertixSecondary)
        }
        Text(
          text = currentValue,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = VertixSecondary,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Siguiente", tint = VertixSecondary)
        }
      }
    }
  }
}
