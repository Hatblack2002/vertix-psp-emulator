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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PspGame
import com.example.ui.components.CompatibilityBadge
import com.example.ui.components.GameCoverImage
import com.example.ui.components.EmptyLibraryState
import com.example.ui.components.VertixBrandLogo
import com.example.ui.components.VertixPrimaryButton
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary
import com.example.viewmodel.VertixScreen

@Composable
fun HomeScreen(
  games: List<PspGame>,
  lastGame: PspGame?,
  onGameClick: (PspGame) -> Unit,
  onResumeClick: (PspGame) -> Unit,
  onImportClick: () -> Unit,
  onNavigateTo: (VertixScreen) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
  ) {
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        VertixBrandLogo(iconSize = 34.dp, showDescriptor = true)

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { onNavigateTo(VertixScreen.LIBRARY) },
            modifier = Modifier.testTag("home_search_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Buscar",
              tint = VertixTextPrimary
            )
          }

          IconButton(
            onClick = { onNavigateTo(VertixScreen.DESIGN_SYSTEM) },
            modifier = Modifier.testTag("home_design_system_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Visibility,
              contentDescription = "Sistema de Diseño",
              tint = VertixSecondary
            )
          }

          IconButton(
            onClick = { onNavigateTo(VertixScreen.SETTINGS) },
            modifier = Modifier.testTag("home_settings_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Ajustes",
              tint = VertixTextSecondary
            )
          }
        }
      }
    }

    if (games.isEmpty()) {
      item {
        EmptyLibraryState(
          onAddGamesClick = onImportClick
        )
      }
    } else {
      if (lastGame != null) {
        item {
          Text(
            text = "Continuar jugando",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = VertixTextPrimary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, VertixBorder, RoundedCornerShape(16.dp))
              .clickable { onGameClick(lastGame) }
              .testTag("continue_game_card"),
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
                  .width(88.dp)
                  .height(118.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .border(0.5.dp, VertixBorder, RoundedCornerShape(10.dp))
              ) {
                GameCoverImage(
                  game = lastGame,
                  contentDescription = lastGame.title,
                  modifier = Modifier.fillMaxSize()
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
              ) {
                CompatibilityBadge(status = lastGame.status)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = lastGame.title,
                  fontFamily = OrbitronFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = VertixTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "Última sesión: ${lastGame.lastPlayed}",
                  fontSize = 12.sp,
                  color = VertixTextSecondary
                )

                Text(
                  text = "Formato: ${lastGame.format} • ${lastGame.size}",
                  fontSize = 12.sp,
                  color = VertixSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                VertixPrimaryButton(
                  text = "Jugar",
                  icon = Icons.Default.PlayArrow,
                  onClick = { onResumeClick(lastGame) },
                  testTag = "continue_playing_button"
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(26.dp))
        }
      }

      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Juegos de la biblioteca",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = VertixTextPrimary,
            letterSpacing = 0.5.sp
          )

          Text(
            text = "Ver todos (${games.size})",
            fontSize = 12.sp,
            color = VertixSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onNavigateTo(VertixScreen.LIBRARY) }
          )
        }

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          items(games) { game ->
            RecentGameItem(game = game, onClick = { onGameClick(game) })
          }
        }

        Spacer(modifier = Modifier.height(28.dp))
      }

      item {
        Text(
          text = "Accesos rápidos",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = VertixTextPrimary,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickAccessTile(
            icon = Icons.Default.VideoLibrary,
            title = "Biblioteca",
            subtitle = "${games.size} títulos",
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(VertixScreen.LIBRARY) }
          )
          QuickAccessTile(
            icon = Icons.Default.Folder,
            title = "Importar",
            subtitle = "Archivos ISO/CSO",
            modifier = Modifier.weight(1f),
            onClick = onImportClick
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickAccessTile(
            icon = Icons.Default.Speed,
            title = "Rendimiento",
            subtitle = "Telemetría nativa",
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(VertixScreen.PERFORMANCE) }
          )
          QuickAccessTile(
            icon = Icons.Default.GraphicEq,
            title = "Ajustes",
            subtitle = "Gráficos y audio",
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(VertixScreen.SETTINGS) }
          )
        }
      }
    }
  }
}

@Composable
fun RecentGameItem(
  game: PspGame,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(120.dp)
      .clickable(onClick = onClick)
      .testTag("recent_game_${game.id}")
  ) {
    Box(
      modifier = Modifier
        .width(120.dp)
        .height(160.dp)
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, VertixBorder, RoundedCornerShape(12.dp))
    ) {
      GameCoverImage(
        game = game,
        contentDescription = game.title,
        modifier = Modifier.fillMaxSize()
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .align(Alignment.BottomCenter)
          .background(
            Brush.verticalGradient(
              listOf(Color.Transparent, VertixBg.copy(alpha = 0.9f))
            )
          )
      )

      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(6.dp)
      ) {
        CompatibilityBadge(status = game.status)
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = game.title,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = VertixTextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

    Text(
      text = "${game.format} • ${game.size}",
      fontSize = 11.sp,
      color = VertixTextSecondary,
      maxLines = 1
    )
  }
}

@Composable
fun QuickAccessTile(
  icon: ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
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
          .size(36.dp)
          .clip(CircleShape)
          .background(VertixSurfaceTertiary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = VertixSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column {
        Text(
          text = title,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = VertixTextPrimary
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = VertixTextSecondary
        )
      }
    }
  }
}
