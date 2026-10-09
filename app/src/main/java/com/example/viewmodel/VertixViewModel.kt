package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.EmulationLogEntry
import com.example.model.LogCategory
import com.example.model.PerformanceMetrics
import com.example.model.PspGame
import com.example.engine.PspLauncher
import com.example.storage.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VertixScreen {
  SPLASH,
  HOME,
  LIBRARY,
  GAME_DETAIL,
  SETTINGS,
  FILES,
  PERFORMANCE,
  DESIGN_SYSTEM
}

enum class LibraryViewMode {
  GRID,
  LIST
}

data class VertixUiState(
  val currentScreen: VertixScreen = VertixScreen.SPLASH,
  val previousScreen: VertixScreen = VertixScreen.HOME,
  val isSplashLoading: Boolean = true,
  val splashStepMessage: String = "Inicializando núcleo PPSSPP...",
  val games: List<PspGame> = emptyList(),
  val selectedGame: PspGame? = null,
  val searchQuery: String = "",
  val selectedCategory: String = "Todos",
  val viewMode: LibraryViewMode = LibraryViewMode.GRID,
  val deviceMetrics: PerformanceMetrics = PerformanceMetrics(),
  val isImporting: Boolean = false,
  val logs: List<EmulationLogEntry> = emptyList(),
  val selectedLogCategory: LogCategory = LogCategory.ALL,
  val logSearchQuery: String = "",
  val toastNotification: String? = null,
  val errorDialogMessage: String? = null
)

class VertixViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = GameRepository(application.applicationContext)
  private val appContext = application.applicationContext

  private val _uiState = MutableStateFlow(VertixUiState())
  val uiState: StateFlow<VertixUiState> = _uiState.asStateFlow()

  init {
    loadPersistedGames()
    startSplashSequence()
    refreshDeviceMetrics()
  }

  private fun loadPersistedGames() {
    val saved = repository.getSavedGames()
    _uiState.update {
      it.copy(
        games = saved,
        selectedGame = saved.firstOrNull()
      )
    }
  }

  private fun startSplashSequence() {
    viewModelScope.launch {
      delay(400)
      _uiState.update { it.copy(splashStepMessage = "Cargando biblioteca...") }
      delay(400)
      _uiState.update { it.copy(splashStepMessage = "Listo") }
      delay(250)
      _uiState.update {
        it.copy(
          isSplashLoading = false,
          currentScreen = VertixScreen.HOME
        )
      }
    }
  }

  /**
   * Métricas REALES del dispositivo (memoria JVM y núcleos disponibles).
   * Se refrescan al abrir la pantalla de rendimiento y al importar/jugar.
   */
  fun refreshDeviceMetrics() {
    val runtime = Runtime.getRuntime()
    val totalMem = runtime.totalMemory() / (1024 * 1024)
    val freeMem = runtime.freeMemory() / (1024 * 1024)
    _uiState.update {
      it.copy(
        deviceMetrics = PerformanceMetrics(
          ramUsedMb = totalMem - freeMem,
          ramTotalMb = runtime.maxMemory() / (1024 * 1024),
          availableCores = runtime.availableProcessors(),
          libraryGames = it.games.size
        )
      )
    }
  }

  fun importGames(uris: List<Uri>) {
    viewModelScope.launch {
      _uiState.update { it.copy(isImporting = true, toastNotification = null) }
      var importedCount = 0
      var lastError = ""
      withContext(Dispatchers.IO) {
        for (uri in uris) {
          val game = repository.importFromUri(uri)
          if (game != null) {
            importedCount++
            addLog(LogCategory.FILES, "INFO", "Juego importado: ${game.title} (${game.format}, ${game.size}) — copia local lista")
          } else {
            lastError = "No se pudo copiar/leer el archivo. ¿Es un ISO/CSO/PBP de PSP válido?"
            addLog(LogCategory.FILES, "ERR", lastError)
          }
        }
      }
      val updatedList = repository.getSavedGames()
      _uiState.update {
        it.copy(
          isImporting = false,
          games = updatedList,
          selectedGame = updatedList.firstOrNull(),
          toastNotification = when {
            importedCount > 0 -> "$importedCount juego(s) importado(s) — portada extraída del archivo cuando estaba disponible"
            else -> lastError.ifBlank { "No se pudo importar el archivo" }
          }
        )
      }
      refreshDeviceMetrics()
    }
  }

  fun removeGame(gameId: String) {
    val updated = repository.removeGame(gameId)
    _uiState.update {
      it.copy(
        games = updated,
        selectedGame = updated.firstOrNull(),
        currentScreen = if (it.currentScreen == VertixScreen.GAME_DETAIL) VertixScreen.LIBRARY else it.currentScreen,
        toastNotification = "Juego y su copia local eliminados"
      )
    }
    addLog(LogCategory.FILES, "INFO", "Juego eliminado de la biblioteca: $gameId")
    refreshDeviceMetrics()
  }

  fun navigateTo(screen: VertixScreen) {
    if (screen == VertixScreen.PERFORMANCE) refreshDeviceMetrics()
    _uiState.update {
      it.copy(
        previousScreen = it.currentScreen,
        currentScreen = screen
      )
    }
  }

  fun selectGame(game: PspGame) {
    _uiState.update {
      it.copy(
        selectedGame = game,
        currentScreen = VertixScreen.GAME_DETAIL
      )
    }
  }

  /**
   * Arranca el juego REAL en el núcleo PPSSPP (PspGameActivity).
   * El archivo debe existir como copia local; si no existe (p. ej. porque
   * el dispositivo borró el cache) se vuelve a copiar desde el URI.
   */
  fun launchGame(game: PspGame) {
    viewModelScope.launch {
      val localPath = withContext(Dispatchers.IO) {
        ensureLocalCopy(game)
      }
      val ok = PspLauncher.launch(appContext, localPath)
      _uiState.update {
        it.copy(
          selectedGame = game,
          toastNotification = if (ok) "Arrancando ${game.title} en el núcleo PPSSPP" else "No se pudo arrancar ${game.title}"
        )
      }
      addLog(
        LogCategory.EMULATION,
        if (ok) "INFO" else "ERR",
        if (ok) {
          "Boot: ${game.title} (${game.format}) — núcleo PPSSPP, ruta=$localPath"
        } else {
          "Fallo de arranque de ${game.title}: archivo no disponible en $localPath"
        }
      )
    }
  }

  private suspend fun ensureLocalCopy(game: PspGame): String? {
    game.localPath?.let { path ->
      val f = java.io.File(path)
      if (f.exists() && f.length() > 0L) return path
    }
    // Re-copia desde el URI original de SAF si sigue disponible.
    return try {
      val uri = Uri.parse(game.filePath)
      val reimported = withContext(Dispatchers.IO) { repository.importFromUri(uri) }
      reimported?.localPath
    } catch (_: Exception) {
      null
    }
  }

  fun toggleFavorite(gameId: String) {
    _uiState.update { state ->
      val updated = state.games.map {
        if (it.id == gameId) it.copy(isFavorite = !it.isFavorite) else it
      }
      repository.saveGames(updated)
      state.copy(
        games = updated,
        selectedGame = if (state.selectedGame?.id == gameId) {
          state.selectedGame.copy(isFavorite = !state.selectedGame.isFavorite)
        } else state.selectedGame
      )
    }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun setSelectedCategory(category: String) {
    _uiState.update { it.copy(selectedCategory = category) }
  }

  fun setViewMode(mode: LibraryViewMode) {
    _uiState.update { it.copy(viewMode = mode) }
  }

  fun setLogCategory(category: LogCategory) {
    _uiState.update { it.copy(selectedLogCategory = category) }
  }

  fun clearLogs() {
    _uiState.update { it.copy(logs = emptyList(), toastNotification = "Registros limpiados") }
  }

  fun clearToast() {
    _uiState.update { it.copy(toastNotification = null) }
  }

  fun showErrorDialog(msg: String) {
    _uiState.update { it.copy(errorDialogMessage = msg) }
  }

  fun clearErrorDialog() {
    _uiState.update { it.copy(errorDialogMessage = null) }
  }

  private fun addLog(category: LogCategory, severity: String, message: String) {
    val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    val entry = EmulationLogEntry(
      id = System.currentTimeMillis().toString(),
      timestamp = timestamp,
      category = category,
      severity = severity,
      message = message
    )
    _uiState.update { it.copy(logs = listOf(entry) + it.logs.take(99)) }
  }
}
