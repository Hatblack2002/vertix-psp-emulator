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
  @DrawableRes val coverResId: Int,
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
  @DrawableRes val thumbnailResId: Int,
  val version: String = "VERTIX v1.0.4"
)

data class GraphicSettings(
  val renderResolution: String = "2x (1080p)",
  val renderingBackend: String = "Vulkan (recomendado)",
  val textureFiltering: Boolean = true,
  val antialiasing: Boolean = true,
  val postProcessing: Boolean = false,
  val fpsLimit: String = "60 FPS",
  val vsync: Boolean = true,
  val aspectRatio: String = "16:9 Estirado"
)

data class AudioSettings(
  val enabled: Boolean = true,
  val volume: Float = 0.85f,
  val latency: String = "Baja (12ms)",
  val audioSync: Boolean = true
)

data class ControlSettings(
  val touchEnabled: Boolean = true,
  val opacity: Float = 0.65f,
  val buttonScale: Float = 1.0f,
  val hapticFeedback: Boolean = true,
  val hideOnGamepad: Boolean = true
)

data class PerformanceMetrics(
  val fps: Float = 58.4f,
  val targetFps: Int = 60,
  val speedPercent: Float = 100.2f,
  val frameTimeMs: Float = 16.9f,
  val cpuUsage: Int = 32,
  val ramUsage: String = "418 MB / 1.8 GB",
  val temperatureC: Int = 36,
  val renderer: String = "Vulkan 1.3"
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
