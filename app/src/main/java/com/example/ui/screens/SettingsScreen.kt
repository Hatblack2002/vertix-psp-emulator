package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

/**
 * Ajustes REALES de VERTIX.
 *
 * El núcleo de emulación es PPSSPP: todos los ajustes de gráficos,
 * audio, controles táctiles y rendimiento se configuran dentro del propio
 * emulador (menú nativo de PPSSPP, accesible con el botón atrás durante el
 * juego) y persisten en ppsspp.ini. Aquí no hay interruptores decorativos:
 * solo información verídica del motor.
 */
@Composable
fun SettingsScreen(
  gameCount: Int,
  onBackClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg)
  ) {
    // Header
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
        fontSize = 17.sp,
        color = VertixTextPrimary
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        SettingsCard(
          icon = Icons.Default.SportsEsports,
          title = "Núcleo de emulación",
          lines = listOf(
            "Motor: PPSSPP (núcleo C++ real, GPL v2+, compilado desde la fuente oficial)",
            "Librería nativa: libppsspp_jni.so (arm64-v8a y armeabi-v7a)",
            "Renderizado: OpenGL ES / Vulkan (según el dispositivo)",
            "Audio: OpenSL ES · CPU emulada: MIPS R4000 (JIT dinámico)"
          )
        )
      }

      item {
        SettingsCard(
          icon = Icons.Default.Settings,
          title = "Dónde se configuran los gráficos, audio y controles",
          lines = listOf(
            "1. Inicia cualquier juego de tu biblioteca.",
            "2. Pulsa el botón atrás para abrir el menú del emulador.",
            "3. Entra en Ajustes: resolución, filtros, backend gráfico, " +
              "controles en pantalla (estilo PSP, opacidad y tamaño), " +
              "audio y velocidad.",
            "Los cambios se guardan automáticamente en ppsspp.ini."
          )
        )
      }

      item {
        SettingsCard(
          icon = Icons.Default.Gamepad,
          title = "Controles en el juego",
          lines = listOf(
            "Durante el juego se superponen los controles clásicos de la " +
              "PSP sobre la pantalla: stick analógico y cruceta a la " +
              "izquierda, botones △ ○ ✕ □ y gatillos a la derecha, Start y " +
              "Select — los posiciona el propio núcleo, en horizontal " +
              "(modo película).",
            "Si tienes un mando Bluetooth, el núcleo lo detecta y " +
              "oculta los controles táctiles."
          )
        )
      }

      item {
        SettingsCard(
          icon = Icons.Default.Info,
          title = "Biblioteca",
          lines = listOf(
            "Juegos importados: $gameCount",
            "Cada juego se copia al almacenamiento privado de la app para " +
              "garantizar que el núcleo siempre pueda leerlo.",
            "La portada que ves se extrae del propio archivo del juego " +
              "(ICON0.PNG del ISO/PBP).",
            "Los savestates y las partidas guardadas se gestionan dentro " +
              "del emulador y se almacenan junto a su juego."
          )
        )
      }
    }
  }
}

@Composable
private fun SettingsCard(
  icon: ImageVector,
  title: String,
  lines: List<String>
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(0.5.dp, VertixBorder, RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = VertixSurface)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = VertixSecondary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = title,
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = VertixTextPrimary
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      lines.forEach { line ->
        Text(
          text = line,
          fontSize = 12.sp,
          color = VertixTextSecondary,
          lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
      }
    }
  }
}
