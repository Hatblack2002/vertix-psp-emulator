package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PspGame
import com.example.model.SaveStateSlot
import com.example.ui.components.CompatibilityBadge
import com.example.ui.components.VertixPrimaryButton
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixError
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

@Composable
fun GameDetailScreen(
  game: PspGame,
  saveSlots: List<SaveStateSlot>,
  onBackClick: () -> Unit,
  onPlayClick: () -> Unit,
  onResumeSlotClick: (SaveStateSlot) -> Unit,
  onToggleFavorite: () -> Unit,
  onRemoveClick: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg),
    contentPadding = PaddingValues(bottom = 96.dp)
  ) {
    // Top Hero Backdrop & Cover Area
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
      ) {
        // Blurred backdrop
        Image(
          painter = painterResource(id = game.coverResId),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .blur(20.dp)
        )

        // Gradient fade into bottom
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  VertixBg.copy(alpha = 0.5f),
                  VertixBg.copy(alpha = 0.85f),
                  VertixBg
                )
              )
            )
        )

        // Back button
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .padding(16.dp)
            .align(Alignment.TopStart)
            .size(42.dp)
            .clip(CircleShape)
            .background(VertixSurface.copy(alpha = 0.8f))
            .border(0.5.dp, VertixBorder, CircleShape)
            .testTag("detail_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = VertixTextPrimary
          )
        }

        // Hero Content Layout
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomStart)
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.Bottom
        ) {
          // Sharp foreground cover
          Box(
            modifier = Modifier
              .width(110.dp)
              .height(150.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(1.5.dp, VertixBorder, RoundedCornerShape(12.dp))
          ) {
            Image(
              painter = painterResource(id = game.coverResId),
              contentDescription = game.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column {
            CompatibilityBadge(status = game.status)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = game.title,
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = VertixTextPrimary,
              lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              TagPill(text = "PSP")
              TagPill(text = game.genre)
              TagPill(text = "★ ${game.rating}", highlight = true)
            }
          }
        }
      }
    }

    // Play & Action Buttons
    item {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          VertixPrimaryButton(
            text = "Jugar",
            icon = Icons.Default.PlayArrow,
            onClick = onPlayClick,
            modifier = Modifier.weight(1f),
            testTag = "detail_play_button"
          )

          if (game.hasSavestate && saveSlots.isNotEmpty()) {
            Box(
              modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VertixSurfaceSecondary)
                .border(1.dp, VertixSecondary, RoundedCornerShape(12.dp))
                .clickable { onResumeSlotClick(saveSlots.first()) }
                .padding(horizontal = 14.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Restore,
                  contentDescription = null,
                  tint = VertixSecondary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Continuar (R1)",
                  color = VertixSecondary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Action Icons Bar (Favorito, Información, Carpeta, Eliminar)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          DetailActionIcon(
            icon = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = "Favorito",
            tint = if (game.isFavorite) VertixPrimary else VertixTextSecondary,
            onClick = onToggleFavorite
          )
          DetailActionIcon(
            icon = Icons.Default.Info,
            label = "Información",
            tint = VertixTextSecondary,
            onClick = {}
          )
          DetailActionIcon(
            icon = Icons.Default.Folder,
            label = "Carpeta",
            tint = VertixTextSecondary,
            onClick = {}
          )
          DetailActionIcon(
            icon = Icons.Default.Delete,
            label = "Eliminar",
            tint = VertixError,
            onClick = onRemoveClick
          )
        }
      }
    }

    // Synopsis / Description
    if (game.description.isNotEmpty()) {
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          Text(
            text = "Descripción",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = VertixTextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = game.description,
            fontSize = 13.sp,
            color = VertixTextSecondary,
            lineHeight = 19.sp
          )
        }
      }
    }

    // Technical Metadata Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, VertixBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = VertixSurface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Información técnica",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = VertixTextPrimary
          )
          Spacer(modifier = Modifier.height(10.dp))

          MetadataRow(label = "Identificador", value = game.id, isCode = true)
          MetadataRow(label = "Formato de archivo", value = "${game.format} (UDF Image)")
          MetadataRow(label = "Tamaño", value = game.size)
          MetadataRow(label = "Ubicación", value = game.filePath, isCode = true)
          MetadataRow(label = "Última ejecución", value = game.lastPlayed)
          MetadataRow(label = "Tiempo de juego", value = game.playTime)
          MetadataRow(label = "Verificación de integridad", value = "Verificado (SHA-1)", isSuccess = true)
        }
      }
    }

    // Save States Section
    if (saveSlots.isNotEmpty()) {
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          Text(
            text = "Estados de emulación guardados",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = VertixTextPrimary
          )
          Spacer(modifier = Modifier.height(10.dp))
        }
      }

      items(saveSlots) { slot ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, VertixBorder, RoundedCornerShape(10.dp))
            .clickable { onResumeSlotClick(slot) },
          colors = CardDefaults.cardColors(containerColor = VertixSurfaceSecondary)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .width(64.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
            ) {
              Image(
                painter = painterResource(id = slot.thumbnailResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Ranura ${slot.slotNumber}: ${slot.title}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = VertixTextPrimary
              )
              Text(
                text = "${slot.timestamp} • ${slot.version}",
                fontSize = 11.sp,
                color = VertixTextSecondary
              )
            }

            Text(
              text = "Cargar",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = VertixSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
fun TagPill(
  text: String,
  highlight: Boolean = false
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (highlight) VertixSurfaceTertiary else VertixSurfaceSecondary)
      .border(0.5.dp, if (highlight) VertixSecondary else VertixBorder, RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Text(
      text = text,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = if (highlight) VertixSecondary else VertixTextSecondary
    )
  }
}

@Composable
fun DetailActionIcon(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(8.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = tint,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      color = tint
    )
  }
}

@Composable
fun MetadataRow(
  label: String,
  value: String,
  isCode: Boolean = false,
  isSuccess: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = VertixTextSecondary
    )
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = if (isCode) FontWeight.Medium else FontWeight.Normal,
      fontFamily = if (isCode) androidx.compose.ui.text.font.FontFamily.Monospace else null,
      color = if (isSuccess) Color(0xFF35D6A0) else if (isCode) VertixSecondary else VertixTextPrimary,
      maxLines = 1,
      overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
      modifier = Modifier.padding(start = 16.dp)
    )
  }
}
