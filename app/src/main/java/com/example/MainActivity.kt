package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TechnicalErrorDialog
import com.example.ui.screens.DesignSystemScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.GameDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VertixBg
import com.example.ui.theme.VertixBorder
import com.example.ui.theme.VertixSecondary
import com.example.ui.theme.VertixSurface
import com.example.ui.theme.VertixSurfaceTertiary
import com.example.ui.theme.VertixTextPrimary
import com.example.ui.theme.VertixTextSecondary
import com.example.viewmodel.VertixScreen
import com.example.viewmodel.VertixViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        VertixApp()
      }
    }
  }
}

@Composable
fun VertixApp(viewModel: VertixViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }

  val openDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris ->
    if (uris.isNotEmpty()) {
      viewModel.importGames(uris)
    }
  }

  fun triggerImport() {
    openDocumentLauncher.launch(arrayOf("*/*"))
  }

  LaunchedEffect(uiState.toastNotification) {
    uiState.toastNotification?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      viewModel.clearToast()
    }
  }

  BackHandler(enabled = uiState.currentScreen != VertixScreen.HOME && uiState.currentScreen != VertixScreen.SPLASH) {
    when (uiState.currentScreen) {
      VertixScreen.GAME_DETAIL -> viewModel.navigateTo(VertixScreen.LIBRARY)
      else -> viewModel.navigateTo(VertixScreen.HOME)
    }
  }

  val showBottomNav = uiState.currentScreen != VertixScreen.SPLASH

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(VertixBg),
    containerColor = VertixBg,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (showBottomNav) {
        VertixBottomNavigationBar(
          currentScreen = uiState.currentScreen,
          onSelectScreen = { viewModel.navigateTo(it) }
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (uiState.currentScreen) {
        VertixScreen.SPLASH -> {
          SplashScreen(
            stepMessage = uiState.splashStepMessage,
            onEnterClick = { viewModel.navigateTo(VertixScreen.HOME) }
          )
        }

        VertixScreen.HOME -> {
          HomeScreen(
            games = uiState.games,
            lastGame = uiState.selectedGame ?: uiState.games.firstOrNull(),
            onGameClick = { viewModel.selectGame(it) },
            onResumeClick = { viewModel.launchGame(it) },
            onImportClick = { triggerImport() },
            onNavigateTo = { viewModel.navigateTo(it) }
          )
        }

        VertixScreen.LIBRARY -> {
          LibraryScreen(
            games = uiState.games,
            searchQuery = uiState.searchQuery,
            selectedCategory = uiState.selectedCategory,
            viewMode = uiState.viewMode,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onCategorySelect = { viewModel.setSelectedCategory(it) },
            onViewModeToggle = { viewModel.setViewMode(it) },
            onGameClick = { viewModel.selectGame(it) },
            onPlayGame = { viewModel.launchGame(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onRemoveGame = { viewModel.removeGame(it) },
            onAddFolderClick = { triggerImport() }
          )
        }

        VertixScreen.GAME_DETAIL -> {
          uiState.selectedGame?.let { game ->
            GameDetailScreen(
              game = game,
              saveSlots = emptyList(),
              onBackClick = { viewModel.navigateTo(VertixScreen.LIBRARY) },
              onPlayClick = { viewModel.launchGame(game) },
              onResumeSlotClick = { viewModel.launchGame(game) },
              onToggleFavorite = { viewModel.toggleFavorite(game.id) },
              onRemoveClick = { viewModel.removeGame(game.id) }
            )
          } ?: run {
            viewModel.navigateTo(VertixScreen.LIBRARY)
          }
        }

        VertixScreen.SETTINGS -> {
          SettingsScreen(
            gameCount = uiState.games.size,
            onBackClick = { viewModel.navigateTo(VertixScreen.HOME) }
          )
        }

        VertixScreen.FILES -> {
          FilesScreen(
            games = uiState.games,
            onBackClick = { viewModel.navigateTo(VertixScreen.HOME) },
            onSelectGame = { viewModel.selectGame(it) },
            onImportFilesClick = { triggerImport() }
          )
        }

        VertixScreen.PERFORMANCE -> {
          PerformanceScreen(
            metrics = uiState.deviceMetrics,
            logs = uiState.logs,
            selectedCategory = uiState.selectedLogCategory,
            onSelectCategory = { viewModel.setLogCategory(it) },
            onClearLogs = { viewModel.clearLogs() },
            onRefresh = { viewModel.refreshDeviceMetrics() },
            onBackClick = { viewModel.navigateTo(VertixScreen.HOME) }
          )
        }

        VertixScreen.DESIGN_SYSTEM -> {
          DesignSystemScreen(
            onNavigateToScreen = { viewModel.navigateTo(it) },
            onShowErrorDialogDemo = {
              viewModel.showErrorDialog("Error de lectura: El archivo seleccionado no contiene una cabecera UDF ISO válida.")
            },
            onBackClick = { viewModel.navigateTo(VertixScreen.HOME) }
          )
        }
      }

      uiState.errorDialogMessage?.let { err ->
        TechnicalErrorDialog(
          title = "Error de archivo",
          message = "No se pudo leer el archivo. Comprueba que el archivo esté completo y en formato ISO/CSO compatible.",
          technicalDetails = err,
          onDismiss = { viewModel.clearErrorDialog() }
        )
      }
    }
  }
}

data class NavItem(
  val screen: VertixScreen,
  val label: String,
  val icon: ImageVector,
  val testTag: String
)

@Composable
fun VertixBottomNavigationBar(
  currentScreen: VertixScreen,
  onSelectScreen: (VertixScreen) -> Unit
) {
  val items = listOf(
    NavItem(VertixScreen.HOME, "Inicio", Icons.Default.Home, "nav_home"),
    NavItem(VertixScreen.LIBRARY, "Biblioteca", Icons.Default.VideoLibrary, "nav_library"),
    NavItem(VertixScreen.FILES, "Archivos", Icons.Default.Folder, "nav_files"),
    NavItem(VertixScreen.PERFORMANCE, "Rendimiento", Icons.Default.Speed, "nav_performance"),
    NavItem(VertixScreen.SETTINGS, "Ajustes", Icons.Default.Settings, "nav_settings")
  )

  NavigationBar(
    modifier = Modifier
      .fillMaxWidth()
      .border(0.5.dp, VertixBorder)
      .navigationBarsPadding(),
    containerColor = VertixSurface,
    tonalElevation = 8.dp
  ) {
    items.forEach { item ->
      val selected = currentScreen == item.screen
      NavigationBarItem(
        icon = {
          Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) VertixSecondary else VertixTextSecondary
          )
        },
        label = {
          Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) VertixTextPrimary else VertixTextSecondary
          )
        },
        selected = selected,
        onClick = { onSelectScreen(item.screen) },
        colors = NavigationBarItemDefaults.colors(
          indicatorColor = VertixSurfaceTertiary,
          selectedIconColor = VertixSecondary,
          unselectedIconColor = VertixTextSecondary,
          selectedTextColor = VertixTextPrimary,
          unselectedTextColor = VertixTextSecondary
        ),
        modifier = Modifier.testTag(item.testTag)
      )
    }
  }
}
