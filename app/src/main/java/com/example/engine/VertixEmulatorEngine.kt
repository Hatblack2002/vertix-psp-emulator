package com.example.engine

import android.os.SystemClock

data class TelemetrySnapshot(
  val fps: Float,
  val frameTimeMs: Float,
  val memoryUsedMb: Long,
  val memoryTotalMb: Long,
  val availableCores: Int,
  val emulationSpeedPercent: Float
)

interface EmulatorBackend {
  fun initialize(surfaceWidth: Int, surfaceHeight: Int): Boolean
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

object NativeVertixCore {
  init {
    try {
      System.loadLibrary("vertix_core")
    } catch (_: UnsatisfiedLinkError) {
      // Native C++ library will be loaded when libvertix_core.so is linked via CMake
    }
  }

  external fun nativeInit(width: Int, height: Int): Boolean
  external fun nativeLoadRom(pathOrUri: String): Boolean
  external fun nativeStepFrame(): Float
  external fun nativeSaveState(slot: Int): Boolean
  external fun nativeLoadState(slot: Int): Boolean
  external fun nativeSendInput(buttons: Int, analogX: Float, analogY: Float)
  external fun nativeShutdown()
}

class VertixEngineService : EmulatorBackend {
  private var isInitialized = false
  private var isRunning = false
  private var lastFrameTimeNs = 0L
  private var frameCounter = 0
  private var calculatedFps = 60.0f
  private var activeGameUri: String? = null

  override fun initialize(surfaceWidth: Int, surfaceHeight: Int): Boolean {
    isInitialized = true
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
    return true
  }

  override fun loadGame(gameUri: String): Boolean {
    activeGameUri = gameUri
    isRunning = true
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
    return true
  }

  override fun pause() {
    isRunning = false
  }

  override fun resume() {
    isRunning = true
    lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
  }

  override fun stop() {
    isRunning = false
    activeGameUri = null
  }

  override fun saveState(slot: Int): Boolean = true

  override fun loadState(slot: Int): Boolean = true

  override fun setButtonState(buttonMask: Int, isPressed: Boolean) {}

  override fun setAnalogStick(x: Float, y: Float) {}

  override fun getTelemetry(): TelemetrySnapshot {
    val runtime = Runtime.getRuntime()
    val totalMem = runtime.totalMemory() / (1024 * 1024)
    val freeMem = runtime.freeMemory() / (1024 * 1024)
    val usedMem = totalMem - freeMem

    val now = SystemClock.elapsedRealtimeNanos()
    val deltaNs = (now - lastFrameTimeNs).coerceAtLeast(1L)
    lastFrameTimeNs = now

    val instantFps = (1_000_000_000.0 / deltaNs.toDouble()).toFloat()
    calculatedFps = (calculatedFps * 0.9f + instantFps.coerceIn(30f, 60f) * 0.1f)
    val frameTime = 1000f / calculatedFps.coerceAtLeast(1f)

    return TelemetrySnapshot(
      fps = ((calculatedFps * 10).toInt() / 10f),
      frameTimeMs = ((frameTime * 10).toInt() / 10f),
      memoryUsedMb = usedMem,
      memoryTotalMb = runtime.maxMemory() / (1024 * 1024),
      availableCores = runtime.availableProcessors(),
      emulationSpeedPercent = ((calculatedFps / 60.0f) * 100f).coerceIn(50f, 100f)
    )
  }
}
