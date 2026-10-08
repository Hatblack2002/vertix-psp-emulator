package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

@Composable
fun PerformanceScreen(
  metrics: PerformanceMetrics,
  fpsHistory: List<Float>,
  logs: List<EmulationLogEntry>,
  selectedCategory: LogCategory,
  onSelectCategory: (LogCategory) -> Unit,
  onClearLogs: () -> Unit,
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

      Icon(
        imageVector = Icons.Default.Speed,
        contentDescription = null,
        tint = VertixSecondary,
        modifier = Modifier.size(24.dp)
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Main FPS Card with Live Timeline Chart
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VertixBorder, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "FPS", fontSize = 11.sp, color = VertixTextSecondary)
                Row(verticalAlignment = Alignment.Bottom) {
                  Text(
                    text = "${metrics.fps}",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = VertixTextPrimary
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "● Estable",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF35D6A0),
                    modifier = Modifier.padding(bottom = 4.dp)
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Objetivo: ${metrics.targetFps} FPS",
                  fontSize = 11.sp,
                  color = VertixTextSecondary
                )
                Text(
                  text = "Velocidad: ${(metrics.speedPercent * 10).toInt() / 10f}%",
                  fontSize = 11.sp,
                  color = VertixSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Live FPS Curve
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VertixBg)
                .border(0.5.dp, VertixBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              FpsTimelineCanvas(history = fpsHistory)
            }
          }
        }
      }

      // Telemetry Metric Tiles (CPU, RAM, Temp, FrameTime)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricTile(
            label = "Uso de CPU",
            value = "${metrics.cpuUsage}%",
            subtext = "ARM64 4 núcleos",
            modifier = Modifier.weight(1f)
          )
          MetricTile(
            label = "Uso de RAM",
            value = metrics.ramUsage,
            subtext = "Memoria del proceso",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricTile(
            label = "Temperatura",
            value = "${metrics.temperatureC}°C",
            subtext = "SoC Óptimo",
            modifier = Modifier.weight(1f)
          )
          MetricTile(
            label = "Tiempo de frame",
            value = "${metrics.frameTimeMs} ms",
            subtext = metrics.renderer,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Emulation Log Header & Controls
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Registro de emulación",
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

      // Log Category Filter Chips
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(LogCategory.values()) { category ->
            FilterChipTab(
              text = category.label,
              selected = selectedCategory == category,
              onClick = { onSelectCategory(category) }
            )
          }
        }
      }

      // Log Entries
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
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              color = VertixTextSecondary,
              modifier = Modifier.width(78.dp)
            )
            Text(
              text = "[${log.category.name}]",
              fontSize = 11.sp,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              color = VertixSecondary,
              modifier = Modifier.width(82.dp)
            )
            Text(
              text = log.message,
              fontSize = 11.sp,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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
fun FpsTimelineCanvas(history: List<Float>) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    if (history.size < 2) return@Canvas
    val w = size.width
    val h = size.height
    val minFps = 45f
    val maxFps = 65f

    val stepX = w / (history.size - 1)
    val points = history.mapIndexed { index, fps ->
      val normalizedY = (fps.coerceIn(minFps, maxFps) - minFps) / (maxFps - minFps)
      Offset(index * stepX, h - (normalizedY * h))
    }

    // Path
    val path = Path().apply {
      moveTo(points.first().x, points.first().y)
      for (i in 1 until points.size) {
        lineTo(points[i].x, points[i].y)
      }
    }

    // Fill under path
    val fillPath = Path().apply {
      addPath(path)
      lineTo(points.last().x, h)
      lineTo(points.first().x, h)
      close()
    }

    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(Color(0x3500E5FF), Color.Transparent),
        startY = 0f,
        endY = h
      )
    )

    // Stroke
    drawPath(
      path = path,
      brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF7B3DFF), Color(0xFF00E5FF))
      ),
      style = Stroke(width = 3.dp.toPx())
    )
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
