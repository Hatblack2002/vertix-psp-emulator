package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompatibilityStatus
import com.example.ui.components.CompatibilityBadge
import com.example.ui.components.VertixBrandLogo
import com.example.ui.components.VertixGeometricIcon
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
import com.example.viewmodel.VertixScreen

data class DesignDeliverable(
  val number: Int,
  val title: String,
  val category: String,
  val targetScreen: VertixScreen? = null
)

@Composable
fun DesignSystemScreen(
  onNavigateToScreen: (VertixScreen) -> Unit,
  onShowErrorDialogDemo: () -> Unit,
  onBackClick: () -> Unit
) {
  val deliverables = listOf(
    DesignDeliverable(1, "Logotipo y sistema de marca", "Identidad", VertixScreen.DESIGN_SYSTEM),
    DesignDeliverable(2, "Paleta de colores y tipografía", "Tokens", VertixScreen.DESIGN_SYSTEM),
    DesignDeliverable(3, "Pantalla de inicio (Splash)", "Flujo de arranque", VertixScreen.SPLASH),
    DesignDeliverable(4, "Pantalla principal (Dashboard)", "Navegación", VertixScreen.HOME),
    DesignDeliverable(5, "Biblioteca en cuadrícula", "Catálogo", VertixScreen.LIBRARY),
    DesignDeliverable(6, "Biblioteca en lista", "Catálogo", VertixScreen.LIBRARY),
    DesignDeliverable(7, "Detalles del juego", "Ficha técnica", VertixScreen.GAME_DETAIL),
    DesignDeliverable(8, "Pantalla de juego horizontal", "In-Game 60 FPS", VertixScreen.GAME_RUNNING),
    DesignDeliverable(9, "Controles táctiles virtuales", "Entrada interactiva", VertixScreen.GAME_RUNNING),
    DesignDeliverable(10, "Menú rápido durante la partida", "In-Game Modal", VertixScreen.GAME_RUNNING),
    DesignDeliverable(11, "Ajustes gráficos (Vulkan)", "Configuración", VertixScreen.SETTINGS),
    DesignDeliverable(12, "Ajustes de audio (Buffer/Sync)", "Configuración", VertixScreen.SETTINGS),
    DesignDeliverable(13, "Configuración de controles", "Configuración", VertixScreen.SETTINGS),
    DesignDeliverable(14, "Gestor de archivos e ISOs", "Almacenamiento", VertixScreen.FILES),
    DesignDeliverable(15, "Gestor de partidas guardadas", "Savestates & Memory Stick", VertixScreen.GAME_DETAIL),
    DesignDeliverable(16, "Estadísticas y diagnóstico", "Telemetría & Logs", VertixScreen.PERFORMANCE),
    DesignDeliverable(17, "Ajustes generales del sistema", "Configuración", VertixScreen.SETTINGS),
    DesignDeliverable(18, "Diálogos de confirmación y error", "Componentes", null),
    DesignDeliverable(19, "Estados vacíos (Empty state)", "Componentes", VertixScreen.LIBRARY),
    DesignDeliverable(20, "Variantes adaptables de pantalla", "Responsive Layout", VertixScreen.HOME)
  )

  val colorTokens = listOf(
    Pair("Fondo principal", Pair("#080F16", VertixBg)),
    Pair("Superficie 1", Pair("#101A27", VertixSurface)),
    Pair("Superficie 2", Pair("#162334", VertixSurfaceSecondary)),
    Pair("Superficie 3", Pair("#1D2C40", VertixSurfaceTertiary)),
    Pair("Violeta principal", Pair("#7B3DFF", VertixPrimary)),
    Pair("Cian secundario", Pair("#00E5FF", VertixSecondary)),
    Pair("Texto principal", Pair("#F0F4FC", VertixTextPrimary)),
    Pair("Texto secundario", Pair("#9CAEC4", VertixTextSecondary)),
    Pair("Borde", Pair("#26384F", VertixBorder)),
    Pair("Éxito / Jugable", Pair("#35D6A0", Color(0xFF35D6A0))),
    Pair("Advertencia", Pair("#FFBE63", Color(0xFFFFBE63))),
    Pair("Error", Pair("#FF647C", Color(0xFFFF647C)))
  )

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
        text = "Sistema de Diseño VERTIX",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = VertixTextPrimary
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Brand Hero Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VertixBorder, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            VertixBrandLogo(iconSize = 56.dp, showDescriptor = true, horizontal = false)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Tu biblioteca PSP, en la palma de tu mano.",
              fontSize = 13.sp,
              color = VertixSecondary,
              fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Especificación Maestra v1.0 — 20 Entregables Visuales Implementados",
              fontSize = 11.sp,
              color = VertixTextSecondary
            )
          }
        }
      }

      // 1. Color Palette Tokens Section
      item {
        Text(
          text = "Paleta Cromática (Tokens)",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = VertixTextPrimary
        )
      }

      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, VertixBorder, RoundedCornerShape(14.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            colorTokens.chunked(2).forEach { pair ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                pair.forEach { (tokenName, tokenData) ->
                  val (hex, color) = tokenData
                  Row(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(VertixSurfaceSecondary)
                      .border(0.5.dp, VertixBorder, RoundedCornerShape(8.dp))
                      .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(text = tokenName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VertixTextPrimary)
                      Text(text = hex, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = VertixSecondary)
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 2. Typography Hierarchy Section
      item {
        Text(
          text = "Tipografía del Sistema",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = VertixTextPrimary
        )
      }

      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, VertixBorder, RoundedCornerShape(14.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Orbitron (Títulos y Gaming)",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = VertixPrimary
            )
            Text(
              text = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z  0 1 2 3 4 5 6 7 8 9",
              fontFamily = OrbitronFontFamily,
              fontSize = 11.sp,
              color = VertixTextSecondary,
              letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Inter (Cuerpo, UI y Metadatos)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = VertixSecondary
            )
            Text(
              text = "Diseñada para legibilidad óptima en interfaces táctiles de alta densidad y áreas seguras.",
              fontSize = 12.sp,
              color = VertixTextPrimary,
              lineHeight = 17.sp
            )
          }
        }
      }

      // 3. Components Preview Section
      item {
        Text(
          text = "Componentes y Estados",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = VertixTextPrimary
        )
      }

      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, VertixBorder, RoundedCornerShape(14.dp)),
          colors = CardDefaults.cardColors(containerColor = VertixSurface)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              CompatibilityBadge(status = CompatibilityStatus.PERFECT)
              CompatibilityBadge(status = CompatibilityStatus.PLAYABLE)
              CompatibilityBadge(status = CompatibilityStatus.UNVERIFIED)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              VertixPrimaryButton(
                text = "Botón Primario",
                onClick = {},
                modifier = Modifier.weight(1f)
              )
              VertixSecondaryButton(
                text = "Secundario",
                onClick = {},
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // 4. Index of all 20 Visual Deliverables
      item {
        Text(
          text = "Índice de los 20 Entregables Maestros",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = VertixTextPrimary
        )
      }

      items(deliverables) { item ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, VertixBorder, RoundedCornerShape(10.dp))
            .clickable {
              if (item.targetScreen != null) {
                onNavigateToScreen(item.targetScreen)
              } else {
                onShowErrorDialogDemo()
              }
            },
          colors = CardDefaults.cardColors(containerColor = VertixSurfaceSecondary)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(VertixSurfaceTertiary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${item.number}",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = VertixSecondary
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = VertixTextPrimary
              )
              Text(
                text = item.category,
                fontSize = 11.sp,
                color = VertixTextSecondary
              )
            }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null,
              tint = VertixSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
