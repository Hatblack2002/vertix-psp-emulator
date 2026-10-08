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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompatibilityStatus
import com.example.model.PspGame
import com.example.ui.components.CompatibilityBadge
import com.example.ui.components.EmptyLibraryState
import com.example.ui.components.FilterChipTab
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixError
import com.example.ui.theme.VertixPrimary
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceSecondary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary
import com.example.viewmodel.LibraryViewMode

@Composable
fun LibraryScreen(
  games: List<PspGame>,
  searchQuery: String,
  selectedCategory: String,
  viewMode: LibraryViewMode,
  onSearchQueryChange: (String) -> Unit,
  onCategorySelect: (String) -> Unit,
  onViewModeToggle: (LibraryViewMode) -> Unit,
  onGameClick: (PspGame) -> Unit,
  onPlayGame: (PspGame) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onRemoveGame: (String) -> Unit,
  onAddFolderClick: () -> Unit
) {
  var isSearchExpanded by remember { mutableStateOf(false) }

  val filteredGames = games.filter { game ->
    val matchesSearch = game.title.contains(searchQuery, ignoreCase = true) ||
      game.id.contains(searchQuery, ignoreCase = true) ||
      game.genre.contains(searchQuery, ignoreCase = true)

    val matchesCategory = when (selectedCategory) {
      "Recientes" -> true
      "Favoritos" -> game.isFavorite
      "Compatibles" -> game.status == CompatibilityStatus.PERFECT || game.status == CompatibilityStatus.PLAYABLE
      "Con problemas" -> game.status == CompatibilityStatus.ISSUES
      else -> true
    }
    matchesSearch && matchesCategory
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Biblioteca",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 22.sp,
          letterSpacing = 1.sp,
          color = VertixTextPrimary
        )
        Text(
          text = "${filteredGames.size} juegos en biblioteca",
          fontSize = 12.sp,
          color = VertixSecondary
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { isSearchExpanded = !isSearchExpanded },
          modifier = Modifier.testTag("library_search_toggle")
        ) {
          Icon(
            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
            contentDescription = "Buscar juegos",
            tint = VertixTextPrimary
          )
        }

        IconButton(
          onClick = {
            onViewModeToggle(
              if (viewMode == LibraryViewMode.GRID) LibraryViewMode.LIST else LibraryViewMode.GRID
            )
          },
          modifier = Modifier.testTag("view_mode_toggle")
        ) {
          Icon(
            imageVector = if (viewMode == LibraryViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
            contentDescription = "Cambiar vista",
            tint = VertixTextPrimary
          )
        }

        IconButton(
          onClick = onAddFolderClick,
          modifier = Modifier.testTag("add_folder_button")
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = "Importar juegos",
            tint = VertixSecondary
          )
        }
      }
    }

    if (isSearchExpanded) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Buscar por nombre o formato...", color = VertixTextSecondary, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VertixSecondary) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = VertixTextSecondary)
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("search_text_field"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = VertixSurface,
          unfocusedContainerColor = VertixSurface,
          focusedBorderColor = VertixPrimary,
          unfocusedBorderColor = VertixBorder,
          focusedTextColor = VertixTextPrimary,
          unfocusedTextColor = VertixTextPrimary
        )
      )
    }

    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
      val categories = listOf("Todos", "Recientes", "Favoritos", "Compatibles")
      items(categories) { category ->
        FilterChipTab(
          text = category,
          selected = selectedCategory == category,
          onClick = { onCategorySelect(category) }
        )
      }
    }

    if (filteredGames.isEmpty()) {
      EmptyLibraryState(
        onAddGamesClick = onAddFolderClick,
        modifier = Modifier.padding(top = 40.dp)
      )
    } else {
      if (viewMode == LibraryViewMode.GRID) {
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(filteredGames) { game ->
            LibraryGridCard(
              game = game,
              onClick = { onGameClick(game) },
              onPlayClick = { onPlayGame(game) },
              onToggleFavorite = { onToggleFavorite(game.id) },
              onRemoveClick = { onRemoveGame(game.id) }
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredGames) { game ->
            LibraryListItem(
              game = game,
              onClick = { onGameClick(game) },
              onPlayClick = { onPlayGame(game) },
              onToggleFavorite = { onToggleFavorite(game.id) },
              onRemoveClick = { onRemoveGame(game.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun LibraryGridCard(
  game: PspGame,
  onClick: () -> Unit,
  onPlayClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRemoveClick: () -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, VertixBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("game_grid_card_${game.id}"),
    colors = CardDefaults.cardColors(containerColor = VertixSurface)
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(0.75f)
          .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
      ) {
        Image(
          painter = painterResource(id = game.coverResId),
          contentDescription = game.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                listOf(Color.Transparent, VertixSurface.copy(alpha = 0.95f))
              )
            )
        )

        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
        ) {
          CompatibilityBadge(status = game.status)
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = game.title,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = VertixTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${game.format} • ${game.size}",
            fontSize = 11.sp,
            color = VertixTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Box {
          IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Opciones",
              tint = VertixTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(VertixSurfaceSecondary)
          ) {
            DropdownMenuItem(
              text = { Text("Jugar", color = VertixTextPrimary, fontSize = 13.sp) },
              onClick = {
                menuExpanded = false
                onPlayClick()
              }
            )
            DropdownMenuItem(
              text = { Text("Ver detalles", color = VertixTextPrimary, fontSize = 13.sp) },
              onClick = {
                menuExpanded = false
                onClick()
              }
            )
            DropdownMenuItem(
              text = {
                Text(
                  if (game.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                  color = VertixTextPrimary,
                  fontSize = 13.sp
                )
              },
              onClick = {
                menuExpanded = false
                onToggleFavorite()
              }
            )
            DropdownMenuItem(
              text = { Text("Eliminar de biblioteca", color = VertixError, fontSize = 13.sp) },
              onClick = {
                menuExpanded = false
                onRemoveClick()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun LibraryListItem(
  game: PspGame,
  onClick: () -> Unit,
  onPlayClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRemoveClick: () -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(0.5.dp, VertixBorder, RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag("game_list_item_${game.id}"),
    colors = CardDefaults.cardColors(containerColor = VertixSurface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(54.dp)
          .height(72.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(0.5.dp, VertixBorder, RoundedCornerShape(8.dp))
      ) {
        Image(
          painter = painterResource(id = game.coverResId),
          contentDescription = game.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = game.title,
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = VertixTextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = "${game.id} • ${game.format} • ${game.size}",
          fontSize = 11.sp,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = VertixSecondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          CompatibilityBadge(status = game.status)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = game.lastPlayed,
            fontSize = 11.sp,
            color = VertixTextSecondary
          )
        }
      }

      IconButton(
        onClick = onPlayClick,
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(VertixSurfaceSecondary)
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Jugar",
          tint = VertixPrimary,
          modifier = Modifier.size(22.dp)
        )
      }

      Box {
        IconButton(
          onClick = { menuExpanded = true },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Opciones",
            tint = VertixTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          modifier = Modifier.background(VertixSurfaceSecondary)
        ) {
          DropdownMenuItem(
            text = { Text("Eliminar de biblioteca", color = VertixError, fontSize = 13.sp) },
            onClick = {
              menuExpanded = false
              onRemoveClick()
            }
          )
        }
      }
    }
  }
}
