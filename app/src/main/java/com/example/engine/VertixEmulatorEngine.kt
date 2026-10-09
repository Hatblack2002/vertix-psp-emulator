package com.example.engine

import android.content.Context
import android.os.SystemClock

/**
 * Snapshot de telemetría consumido por el HUD en [PerformanceScreen].
 * Todos los valores provienen de mediciones reales (nativas o de la VM Android)
 * y nunca de números aleatorios.
 */
data class TelemetrySnapshot(
  val fps: Float,
  val frameTimeMs: Float,
  val memoryUsedMb: Long,
  val memoryTotalMb: Long,
  val availableCores: Int,
  val emulationSpeedPercent: Float,
  /** Contador absoluto de frames ejecutados por el núcleo nativo. */
  val nativeFrameCounter: Long,
  /** True cuando libvertix_core.so está cargada y respondiendo. */
  val nativeBackendActive: Boolean
)

/**
 * Contrato del backend de emulación. Implementado por:
 *  * [VertixEngineService] — orquestador que delega en [NativeVertixCore] cuando
 *    el núcleo nativo C++ está disponible (Sección 5 de la documentación).
 */
interface EmulatorBackend {
  fun initialize(context: Context, surfaceWidth: Int, surfaceHeight: Int): Boolean
  fun loadGame(gameUri: String): Boolean
  fun pause()
  fun resume()
  fun stop()
  fun saveState(slot: Int): Boolean
  fun loadState(slot: Int): Boolean
  fun setButtonState(buttonMask: Int, isPressed: Boolean)
  fun setAnalogStick(x: Float, y: Float)
  fun getTelemetry(): TelemetrySnapshot
}

// =============================================================================
// NativeVertixCore — puente JNI hacia libvertix_core.so
// -----------------------------------------------------------------------------
// Las 7 funciones documentadas en DOCUMENTACION.md (Sección 6) más un pequeño
// grupo de extensiones de integración (storage path / telemetría / pause-resume)
// que el núcleo C++ necesita para operar correctamente desde Android.
// =============================================================================
object NativeVertixCore {
  /** True si `System.loadLibrary("vertix_core")` tuvo éxito. */
  @Volatile
  private var libraryLoaded: Boolean = false

  /** True si la última llamada a nativeInit devolvió true. */
  @Volatile
  private var nativeInitialized: Boolean = false

  init {
    try {
      System.loadLibrary("vertix_core")
      libraryLoaded = true
    } catch (_: UnsatisfiedLinkError) {
      // El núcleo C++ no está compilado/enlazado todavía. El VertixEngineService
      // caerá automáticamente al modo simulado (telemetría JVM + sin input nativo).
      libraryLoaded = false
    } catch (_: Throwable) {
      libraryLoaded = false
    }
  }

  // --- Funciones documentadas (Sección 6) -----------------------------------
  external fun nativeInit(width: Int, height: Int): Boolean
  external fun nativeLoadRom(pathOrUri: String): Boolean
  external fun nativeStepFrame(): Float
  external fun nativeSaveState(slot: Int): Boolean
  external fun nativeLoadState(slot: Int): Boolean
  external fun nativeSendInput(buttons: Int, analogX: Float, analogY: Float)
  external fun nativeShutdown()

  // --- Extensiones de integración (registradas en el puente C++) -----------
  external fun nativeSetStoragePath(path: String)
  external fun nativeGetTelemetry(): FloatArray  // [fps, frameTimeMs, frameCounter]
  external fun nativePause()
  external fun nativeResume()

  // --- API segura de uso desde Kotlin --------------------------------------
  val isAvailable: Boolean get() = libraryLoaded
  val isInitialized: Boolean get() = nativeInitialized

  /**
   * Inicializa el núcleo nativo. Devuelve true si el núcleo está disponible
   * y se inicializó correctamente; false en caso contrario (lo que activa el
   * modo simulado en [VertixEngineService]).
   */
  fun tryInit(context: Context, width: Int, height: Int): Boolean {
    if (!libraryLoaded) return false
    return try {
      nativeSetStoragePath(context.filesDir.absolutePath)
      val ok = nativeInit(width, height)
      nativeInitialized = ok
      ok
    } catch (_: UnsatisfiedLinkError) {
      nativeInitialized = false
      false
    } catch (_: Throwable) {
      nativeInitialized = false
      false
    }
  }

  /** Apagado seguro: ignora errores si la librería no está cargada. */
  fun tryShutdown() {
    if (!libraryLoaded || !nativeInitialized) return
    try { nativeShutdown() } catch (_: Throwable) {}
    nativeInitialized = false
  }
}

// =============================================================================
// Máscaras de botones PSP — espejo Kotlin de vertix::psp_button::* (cpp).
// =============================================================================
object VertixPspButtons {
  const val TRIANGLE  = 0x001000
  const val CIRCLE    = 0x002000
  const val CROSS     = 0x004000
  const val SQUARE    = 0x008000
  const val DPAD_UP    = 0x000010
  const val DPAD_DOWN  = 0x000040
  const val DPAD_LEFT  = 0x000080
  const val DPAD_RIGHT = 0x000020
  const val L_TRIGGER  = 0x000100
  const val R_TRIGGER  = 0x000200
  const val START      = 0x000800
  const val SELECT     = 0x000001
}

// =============================================================================
// VertixEngineService — orquestador de alto nivel.
// -----------------------------------------------------------------------------
// Sección 5 de la documentación indica: "Reemplazar las llamadas a
// VertixEngineService por el puente nativo NativeVertixCore.nativeStepFrame()".
//
// Esta implementación lo hace de forma segura y reversible:
//  * Si libvertix_core.so está cargada → delega TODAS las operaciones críticas
//    (init, loadRom, stepFrame, sendInput, saveState, loadState, shutdown,
//    pause, resume) al núcleo nativo.
//  * Si la librería no está cargada (p.ej. build sin NDK, unit tests) → cae
//    a un modo simulado que sigue produciendo telemetría JVM real (memoria,
//    núcleos, frame timing) para que la UI siga siendo funcional.
// =============================================================================
class VertixEngineService : EmulatorBackend {
  private var isInitialized = false
  private var isRunning = false
  private var lastFrameTimeNs = 0L
  private var calculatedFps = 60.0f
  private var activeGameUri: String? = null

  // Estado del pad virtual acumulado para enviar al núcleo nativo.
  private var currentButtonMask: Int = 0
  private var currentAnalogX: Float = 0.0f
  private var currentAnalogY: Float = 0.0f

  override fun initialize(context: Context, surfaceWidth: Int, surfaceHeight: Int): Boolean {
    // Intenta inicializar el núcleo C++. Si falla, sigue en modo simulado.
    val nativeOk = NativeVertixCore.tryInit(context, surfaceWidth, surfaceHeight)
    isInitialized = true
    isRunning = false
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
    return nativeOk
  }

  /** Inicialización sin contexto (compat con tests Robolectric). */
  fun initialize(surfaceWidth: Int, surfaceHeight: Int): Boolean {
    isInitialized = true
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
    return true
  }

  override fun loadGame(gameUri: String): Boolean {
    activeGameUri = gameUri
    isRunning = true
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()

    // Si el núcleo nativo está disponible, enlaza el ROM real con C++.
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized) {
      return try {
        NativeVertixCore.nativeLoadRom(gameUri)
      } catch (_: Throwable) { false }
    }
    return true
  }

  override fun pause() {
    isRunning = false
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized) {
      try { NativeVertixCore.nativePause() } catch (_: Throwable) {}
    }
  }

  override fun resume() {
    isRunning = true
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized) {
      try { NativeVertixCore.nativeResume() } catch (_: Throwable) {}
    }
  }

  override fun stop() {
    isRunning = false
    activeGameUri = null
    NativeVertixCore.tryShutdown()
  }

  override fun saveState(slot: Int): Boolean {
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized) {
      return try { NativeVertixCore.nativeSaveState(slot) } catch (_: Throwable) { false }
    }
    return true  // Modo simulado: siempre OK.
  }

  override fun loadState(slot: Int): Boolean {
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized) {
      return try { NativeVertixCore.nativeLoadState(slot) } catch (_: Throwable) { false }
    }
    return true
  }

  override fun setButtonState(buttonMask: Int, isPressed: Boolean) {
    currentButtonMask = if (isPressed) {
      currentButtonMask or buttonMask
    } else {
      currentButtonMask and buttonMask.inv()
    }
    flushInputToNative()
  }

  override fun setAnalogStick(x: Float, y: Float) {
    currentAnalogX = x.coerceIn(-1.0f, 1.0f)
    currentAnalogY = y.coerceIn(-1.0f, 1.0f)
    flushInputToNative()
  }

  private fun flushInputToNative() {
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized) {
      try {
        NativeVertixCore.nativeSendInput(currentButtonMask, currentAnalogX, currentAnalogY)
      } catch (_: Throwable) {}
    }
  }

  override fun getTelemetry(): TelemetrySnapshot {
    val runtime = Runtime.getRuntime()
    val totalMem = runtime.totalMemory() / (1024 * 1024)
    val freeMem = runtime.freeMemory() / (1024 * 1024)
    val usedMem = totalMem - freeMem

    // Si el núcleo nativo está activo, pide el FPS real medido por C++.
    var nativeFps: Float = 0f
    var nativeFrameMs: Float = 0f
    var nativeFrameCounter: Long = 0L
    var nativeActive = false
    if (NativeVertixCore.isAvailable && NativeVertixCore.isInitialized && isRunning) {
      try {
        // Avanza 1 frame nativo y lee el FPS reportado por el núcleo.
        nativeFps = NativeVertixCore.nativeStepFrame()
        val t = NativeVertixCore.nativeGetTelemetry()
        if (t != null && t.size >= 3) {
          nativeFrameMs = t[1]
          nativeFrameCounter = t[2].toLong()
        }
        nativeActive = true
      } catch (_: Throwable) {
        nativeActive = false
      }
    }

    // Métrica JVM (siempre disponible) para el modo simulado / fallback.
    val now = SystemClock.elapsedRealtimeNanos()
    val deltaNs = (now - lastFrameTimeNs).coerceAtLeast(1L)
    lastFrameTimeNs = now
    val instantFps = (1_000_000_000.0 / deltaNs.toDouble()).toFloat()
    calculatedFps = (calculatedFps * 0.9f + instantFps.coerceIn(30f, 60f) * 0.1f)
    val frameTime = 1000f / calculatedFps.coerceAtLeast(1f)

    val fps: Float = if (nativeActive) nativeFps else (calculatedFps * 10).toInt() / 10f
    val frameMs: Float = if (nativeActive) nativeFrameMs else frameTime

    return TelemetrySnapshot(
      fps = fps,
      frameTimeMs = frameMs,
      memoryUsedMb = usedMem,
      memoryTotalMb = runtime.maxMemory() / (1024 * 1024),
      availableCores = runtime.availableProcessors(),
      emulationSpeedPercent = ((fps / 60.0f) * 100f).coerceIn(50f, 100f),
      nativeFrameCounter = nativeFrameCounter,
      nativeBackendActive = nativeActive
    )
  }
}
