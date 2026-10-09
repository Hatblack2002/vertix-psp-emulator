package com.example.model

import androidx.annotation.DrawableRes

enum class CompatibilityStatus(val label: String, val colorHex: Long) {
  PERFECT("● Perfecto", 0xFF35D6A0),
  PLAYABLE("● Jugable", 0xFF35D6A0),
  UNVERIFIED("● No verificado", 0xFFFFBE63),
  ISSUES("● Con problemas", 0xFFFF647C)
}

data class PspGame(
  val id: String,
  val title: String,
  val subtitle: String,
  val genre: String,
  val rating: String,
  val format: String,
  val size: String,
  /** Recurso legacy (ya no se usan portadas falsas). 0 = sin recurso. */
  @DrawableRes val coverResId: Int = 0,
  /** Ruta de la portada real (ICON0.PNG extraída del ISO/PBP). */
  val coverPath: String? = null,
  /** Ruta local real del archivo de juego (copia privada en filesDir/games). */
  val localPath: String? = null,
  val filePath: String,
  val status: CompatibilityStatus,
  val lastPlayed: String,
  val playTime: String,
  val isFavorite: Boolean = false,
  val description: String = "",
  val hasSavestate: Boolean = false
)

data class SaveStateSlot(
  val id: String,
  val gameId: String,
  val slotNumber: Int,
  val title: String,
  val timestamp: String,
  val version: String = "PPSSPP"
)

data class PerformanceMetrics(
  val ramUsedMb: Long = 0,
  val ramTotalMb: Long = 0,
  val availableCores: Int = 0,
  val libraryGames: Int = 0
)

enum class LogCategory(val label: String) {
  ALL("Todos"),
  SYSTEM("Sistema"),
  EMULATION("Emulación"),
  GRAPHICS("Gráficos"),
  AUDIO("Audio"),
  FILES("Archivos"),
  ERROR("Errores")
}

data class EmulationLogEntry(
  val id: String,
  val timestamp: String,
  val category: LogCategory,
  val severity: String, // "INFO", "WARN", "ERR", "DEBUG"
  val message: String
)
