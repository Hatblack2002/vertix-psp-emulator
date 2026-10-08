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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PspGame
import com.example.ui.components.VertixPrimaryButton
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary

@Composable
fun FilesScreen(
  games: List<PspGame>,
  onBackClick: () -> Unit,
  onSelectGame: (PspGame) -> Unit,
  onImportFilesClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg)
  ) {
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
          text = "Archivos de juegos",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = VertixTextPrimary
        )
      }

      IconButton(onClick = onImportFilesClick) {
        Icon(
          imageVector = Icons.Default.CreateNewFolder,
          contentDescription = "Importar",
          tint = VertixSecondary
        )
      }
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clip(RoundedCornerShape(10.dp))
        .border(0.5.dp, VertixBorder, RoundedCornerShape(10.dp)),
      colors = CardDefaults.cardColors(containerColor = VertixSurface)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Storage,
          contentDescription = null,
          tint = VertixSecondary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Almacenamiento de dispositivo • Selector SAF",
          fontSize = 12.sp,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = VertixTextSecondary
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (games.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 24.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(0.5.dp, VertixBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = VertixSurface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = VertixSecondary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Sin archivos importados",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = VertixTextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Pulsa el botón de abajo para explorar tus carpetas e importar archivos ISO o CSO.",
                fontSize = 13.sp,
                color = VertixTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        items(games) { game ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp))
              .clickable { onSelectGame(game) }
              .testTag("file_item_${game.id}"),
            colors = CardDefaults.cardColors(containerColor = VertixSurface)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(VertixSurfaceTertiary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Folder,
                  contentDescription = null,
                  tint = VertixSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = game.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = VertixTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${game.format} • ${game.size}",
                  fontSize = 11.sp,
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  color = VertixTextSecondary
                )
              }

              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = VertixTextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
        VertixPrimaryButton(
          text = "Seleccionar archivos (ISO / CSO)",
          icon = Icons.Default.FolderOpen,
          onClick = onImportFilesClick,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
