package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmulationLogEntry
import com.example.model.LogCategory
import com.example.model.PerformanceMetrics
import com.example.ui.components.FilterChipTab
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

/**
 * Diagnóstico REAL: métricas medidas del dispositivo (memoria del proceso,
 * núcleos de CPU) y registro de eventos de la app.
 *
 * El FPS y la velocidad de emulación durante el juego los mide y muestra el
 * propio núcleo PPSSPP con su HUD nativo (menú del emulador dentro del juego),
 * porque son los únicos números verídicos del emulador.
 */
@Composable
fun PerformanceScreen(
  metrics: PerformanceMetrics,
  logs: List<EmulationLogEntry>,
  selectedCategory: LogCategory,
  onSelectCategory: (LogCategory) -> Unit,
  onClearLogs: () -> Unit,
  onRefresh: () -> Unit,
  onBackClick: () -> Unit
) {
  val context = LocalContext.current

  val filteredLogs = logs.filter {
    selectedCategory == LogCategory.ALL || it.category == selectedCategory
  }

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
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = VertixTextPrimary
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Rendimiento y Diagnóstico",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = VertixTextPrimary
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refrescar métricas",
            tint = VertixSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = null,
          tint = VertixSecondary,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Dispositivo — valores reales medidos
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VertixBorder, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Dispositivo (medido)",
              fontSize = 12.sp,
              color = VertixTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "RAM del proceso",
                value = "${metrics.ramUsedMb} MB",
                subtext = "de ${metrics.ramTotalMb} MB disponibles",
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "Núcleos CPU",
                value = "${metrics.availableCores}",
                subtext = Build.HARDWARE.ifBlank { "SoC del dispositivo" },
                modifier = Modifier.weight(1f)
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "Juegos en biblioteca",
                value = "${metrics.libraryGames}",
                subtext = "copias locales verificadas",
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "Android",
                value = "API ${Build.VERSION.SDK_INT}",
                subtext = Build.MODEL.ifBlank { "Modelo desconocido" },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // Núcleo de emulación — información real
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VertixBorder, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = VertixTextPrimary
          )
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(listOf(Color(0x1400E5FF), Color(0x147B3DFF)))
              )
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Núcleo de emulación",
                fontSize = 12.sp,
                color = VertixTextSecondary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "PPSSPP v1.19.3",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = VertixTextPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Librería nativa libppsspp_jni.so (C++) compilada desde la " +
                  "fuente oficial de PPSSPP. Los FPS, la velocidad de emulación y " +
                  "el renderizado se miden dentro del propio emulador: durante el " +
                  "juego, abre el menú del emulador (botón atrás) para ver las " +
                  "estadísticas reales.",
                fontSize = 11.sp,
                color = VertixTextSecondary,
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      // Registro — Header & Controls
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Registro de eventos",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = VertixTextPrimary
          )

          Row {
            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = logs.joinToString("\n") { "[${it.timestamp}] [${it.category}] ${it.message}" }
                clipboard.setPrimaryClip(ClipData.newPlainText("VERTIX Logs", text))
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copiar logs", tint = VertixSecondary, modifier = Modifier.size(18.dp))
            }

            IconButton(
              onClick = onClearLogs,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar logs", tint = VertixTextSecondary, modifier = Modifier.size(18.dp))
            }
          }
        }
      }

      // Filtros del registro
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(LogCategory.entries) { category ->
            FilterChipTab(
              text = category.label,
              selected = selectedCategory == category,
              onClick = { onSelectCategory(category) }
            )
          }
        }
      }

      // Entradas del registro
      items(filteredLogs) { log ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, VertixBorder, RoundedCornerShape(8.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = log.timestamp,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = VertixTextSecondary,
              modifier = Modifier.width(78.dp)
            )
            Text(
              text = "[${log.category.name}]",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = VertixSecondary,
              modifier = Modifier.width(82.dp)
            )
            Text(
              text = log.message,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = VertixTextPrimary,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}

@Composable
fun MetricTile(
  label: String,
  value: String,
  subtext: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = VertixSurface)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = label, fontSize = 11.sp, color = VertixTextSecondary)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = VertixTextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = subtext, fontSize = 10.sp, color = VertixSecondary)
    }
  }
}
