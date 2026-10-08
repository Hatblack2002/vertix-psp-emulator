package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.engine.VertixEngineService
import com.example.model.AudioSettings
import com.example.model.ControlSettings
import com.example.model.EmulationLogEntry
import com.example.model.GraphicSettings
import com.example.model.LogCategory
import com.example.model.PerformanceMetrics
import com.example.model.PspGame
import com.example.model.SaveStateSlot
import com.example.storage.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VertixScreen {
  SPLASH,
  HOME,
  LIBRARY,
  GAME_DETAIL,
  GAME_RUNNING,
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
  val splashStepMessage: String = "Inicializando subsistemas...",
  val games: List<PspGame> = emptyList(),
  val selectedGame: PspGame? = null,
  val searchQuery: String = "",
  val selectedCategory: String = "Todos",
  val viewMode: LibraryViewMode = LibraryViewMode.GRID,
  val graphicSettings: GraphicSettings = GraphicSettings(),
  val audioSettings: AudioSettings = AudioSettings(),
  val controlSettings: ControlSettings = ControlSettings(),
  val performanceMetrics: PerformanceMetrics = PerformanceMetrics(),
  val fpsHistory: List<Float> = emptyList(),
  val isGameRunning: Boolean = false,
  val isQuickMenuOpen: Boolean = false,
  val currentSaveSlots: List<SaveStateSlot> = emptyList(),
  val logs: List<EmulationLogEntry> = emptyList(),
  val selectedLogCategory: LogCategory = LogCategory.ALL,
  val logSearchQuery: String = "",
  val toastNotification: String? = null,
  val errorDialogMessage: String? = null
)

class VertixViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = GameRepository(application.applicationContext)
  private val engineService = VertixEngineService()

  private val _uiState = MutableStateFlow(VertixUiState())
  val uiState: StateFlow<VertixUiState> = _uiState.asStateFlow()

  init {
    loadPersistedGames()
    initSystemLogs()
    startSplashSequence()
    startTelemetryMonitor()
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

  private fun initSystemLogs() {
    val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    val initialLogs = listOf(
      EmulationLogEntry("1", timestamp, LogCategory.SYSTEM, "INFO", "VERTIX Android ARM64 Core initialized"),
      EmulationLogEntry("2", timestamp, LogCategory.GRAPHICS, "INFO", "Surface backend: Vulkan 1.3 / OpenGLES"),
      EmulationLogEntry("3", timestamp, LogCategory.AUDIO, "INFO", "Audio subsystem connected via AAudio"),
      EmulationLogEntry("4", timestamp, LogCategory.FILES, "INFO", "Storage Access Framework ready for ISO/CSO")
    )
    _uiState.update { it.copy(logs = initialLogs) }
  }

  private fun startSplashSequence() {
    viewModelScope.launch {
      delay(500)
      _uiState.update { it.copy(splashStepMessage = "Cargando configuración...") }
      delay(400)
      _uiState.update { it.copy(splashStepMessage = "Preparando biblioteca...") }
      delay(400)
      _uiState.update { it.copy(splashStepMessage = "Listo") }
      delay(300)
      _uiState.update {
        it.copy(
          isSplashLoading = false,
          currentScreen = VertixScreen.HOME
        )
      }
    }
  }

  private fun startTelemetryMonitor() {
    viewModelScope.launch {
      while (true) {
        delay(1000)
        val telemetry = engineService.getTelemetry()
        _uiState.update { state ->
          val newHistory = (state.fpsHistory + telemetry.fps).takeLast(20)
          state.copy(
            performanceMetrics = state.performanceMetrics.copy(
              fps = telemetry.fps,
              frameTimeMs = telemetry.frameTimeMs,
              cpuUsage = (telemetry.availableCores * 8).coerceAtMost(100),
              ramUsage = "${telemetry.memoryUsedMb} MB / ${telemetry.memoryTotalMb} MB",
              speedPercent = telemetry.emulationSpeedPercent
            ),
            fpsHistory = newHistory
          )
        }
      }
    }
  }

  fun importGames(uris: List<Uri>) {
    viewModelScope.launch {
      var importedCount = 0
      for (uri in uris) {
        val game = repository.importFromUri(uri)
        if (game != null) {
          importedCount++
          addLog(LogCategory.FILES, "INFO", "Juego importado: ${game.title} (${game.format}, ${game.size})")
        }
      }
      val updatedList = repository.getSavedGames()
      _uiState.update {
        it.copy(
          games = updatedList,
          selectedGame = updatedList.firstOrNull(),
          toastNotification = if (importedCount > 0) "$importedCount juego(s) importado(s) a la biblioteca" else "No se pudo leer el archivo"
        )
      }
    }
  }

  fun removeGame(gameId: String) {
    val updated = repository.removeGame(gameId)
    _uiState.update {
      it.copy(
        games = updated,
        selectedGame = updated.firstOrNull(),
        currentScreen = if (it.currentScreen == VertixScreen.GAME_DETAIL) VertixScreen.LIBRARY else it.currentScreen,
        toastNotification = "Juego eliminado de la biblioteca"
      )
    }
    addLog(LogCategory.FILES, "INFO", "Juego eliminado de la biblioteca: $gameId")
  }

  fun navigateTo(screen: VertixScreen) {
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

  fun launchGame(game: PspGame) {
    engineService.loadGame(game.filePath)
    _uiState.update {
      it.copy(
        selectedGame = game,
        isGameRunning = true,
        currentScreen = VertixScreen.GAME_RUNNING,
        toastNotification = "Iniciando ${game.title}"
      )
    }
    addLog(LogCategory.EMULATION, "INFO", "Núcleo ejecutando ROM: ${game.title} (${game.filePath})")
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

  fun openQuickMenu() {
    engineService.pause()
    _uiState.update { it.copy(isQuickMenuOpen = true) }
  }

  fun closeQuickMenu() {
    engineService.resume()
    _uiState.update { it.copy(isQuickMenuOpen = false) }
  }

  fun saveState() {
    engineService.saveState(1)
    val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    addLog(LogCategory.EMULATION, "INFO", "Estado de emulación guardado en ranura 1 ($now)")
    _uiState.update {
      it.copy(
        isQuickMenuOpen = false,
        toastNotification = "Estado guardado en Ranura 1"
      )
    }
  }

  fun loadState() {
    engineService.loadState(1)
    addLog(LogCategory.EMULATION, "INFO", "Estado de emulación cargado desde ranura 1")
    _uiState.update {
      it.copy(
        isQuickMenuOpen = false,
        toastNotification = "Estado cargado desde Ranura 1"
      )
    }
  }

  fun stopGame() {
    engineService.stop()
    _uiState.update {
      it.copy(
        isGameRunning = false,
        isQuickMenuOpen = false,
        currentScreen = VertixScreen.HOME
      )
    }
  }

  fun updateGraphicSettings(settings: GraphicSettings) {
    _uiState.update { it.copy(graphicSettings = settings) }
  }

  fun updateAudioSettings(settings: AudioSettings) {
    _uiState.update { it.copy(audioSettings = settings) }
  }

  fun updateControlSettings(settings: ControlSettings) {
    _uiState.update { it.copy(controlSettings = settings) }
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

  fun resetAllSettings() {
    _uiState.update {
      it.copy(
        graphicSettings = GraphicSettings(),
        audioSettings = AudioSettings(),
        controlSettings = ControlSettings(),
        toastNotification = "Ajustes restablecidos"
      )
    }
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
