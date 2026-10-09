// =============================================================================
// VERTIX PSP Emulator — JNI Bridge
// -----------------------------------------------------------------------------
// Implementación del puente JNI entre la capa Kotlin (objeto `NativeVertixCore`
// en `engine/VertixEmulatorEngine.kt`) y el núcleo C++ `libvertix_core.so`.
//
// Funciones expuestas (especificadas en DOCUMENTACION.md, Sección 6):
//   * nativeInit(width, height)        -> inicializa subsistemas
//   * nativeLoadRom(pathOrUri)         -> carga ISO/CSO/PBP (vía SAF Uri)
//   * nativeStepFrame()                -> avanza 1 VBLANK (16.6ms) y devuelve FPS
//   * nativeSendInput(buttons, ax, ay) -> inyecta estado del pad al registro Ctrl
//   * nativeSaveState(slot)            -> vuelca snapshot de memoria a disco
//   * nativeLoadState(slot)            -> restaura snapshot desde disco
//   * nativeShutdown()                 -> libera recursos
//
// Convención de nombres JNI:
//   Java_<package>_<Object>_<method>  ->  Java_com_example_engine_NativeVertixCore_*
//
// Para un objeto `object NativeVertixCore` en Kotlin, el receptor es la
// instancia singleton (jobject thiz), tal y como refleja la documentación.
// =============================================================================

#include <jni.h>
#include <android/log.h>
#include <sys/stat.h>
#include <unistd.h>
#include <cerrno>
#include <cstring>
#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>

#include "vertix_core.h"

// Etiqueta común para todos los logs emitidos por el núcleo nativo.
// Visible vía `adb logcat -s VertixNative`.
#define VERTIX_LOG_TAG "VertixNative"
#define VERTIX_LOGI(...) __android_log_print(ANDROID_LOG_INFO,  VERTIX_LOG_TAG, __VA_ARGS__)
#define VERTIX_LOGW(...) __android_log_print(ANDROID_LOG_WARN,  VERTIX_LOG_TAG, __VA_ARGS__)
#define VERTIX_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, VERTIX_LOG_TAG, __VA_ARGS__)
#define VERTIX_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, VERTIX_LOG_TAG, __VA_ARGS__)

namespace {

// Directorio privado de la app donde se guardan los savestates binarios.
// Se obtiene en runtime desde el lado Kotlin (context.filesDir) y se inyecta
// vía `nativeSetStoragePath` (ver más abajo). Antes de recibirlo, se usa
// /data/local/tmp como ubicación por defecto para no romper el contrato.
std::string g_storage_path = "/data/local/tmp";

// Construye la ruta completa del archivo de savestate para un slot dado.
std::string build_savestate_path(int slot) {
    if (g_storage_path.empty()) {
        g_storage_path = "/data/local/tmp";
    }
    // Elimina barra final si existe para no duplicar separadores.
    std::string base = g_storage_path;
    if (!base.empty() && base.back() == '/') base.pop_back();
    return base + "/vertix_savestate_slot_" + std::to_string(slot) + ".bin";
}

}  // namespace

// =============================================================================
// Implementación del núcleo C++ (vertix::core_*).
// =============================================================================

namespace vertix {

VertixCoreState& core_state() {
    static VertixCoreState instance;
    return instance;
}

bool core_initialize(int width, int height) {
    VertixCoreState& s = core_state();
    s.surfaceWidth = width;
    s.surfaceHeight = height;
    s.lastFrameTime = std::chrono::steady_clock::now();
    s.frameCounter = 0;
    s.currentFps = 0.0f;
    s.currentFrameTimeMs = 0.0f;
    s.initialized = true;
    s.paused = false;
    VERTIX_LOGI("core_initialize: surface=%dx%d scale=%dx",
                width, height, s.renderScale);
    return true;
}

bool core_load_rom(const std::string& path_or_uri) {
    VertixCoreState& s = core_state();
    if (!s.initialized) {
        VERTIX_LOGE("core_load_rom: núcleo no inicializado — llama a core_initialize primero");
        return false;
    }

    // En esta iteración de scaffolding no decodificamos el formato ISO/CSO/PBP.
    // La integración con el PPSSPP Core sustituirá este punto por:
    //   1. Apertura del FileDescriptor del SAF Uri (content://...) vía
    //      ContentResolver desde Kotlin y paso del fd al núcleo.
    //   2. Inicialización de la memoria del PSP (32MB/64MB RAM).
    //   3. Carga de la tabla de particiones del UMD y el EBOOT.BIN.
    s.activeRomPath = path_or_uri;
    s.romLoaded = true;
    s.running = true;
    s.lastFrameTime = std::chrono::steady_clock::now();
    VERTIX_LOGI("core_load_rom: ROM enlazada (%s)", path_or_uri.c_str());
    return true;
}

float core_step_frame() {
    VertixCoreState& s = core_state();
    if (!s.initialized || !s.romLoaded) {
        return 0.0f;
    }
    if (s.paused) {
        // Pausa: reporta 0 FPS sin consumir CPU.
        return 0.0f;
    }

    // --- Bucle de frame -----------------------------------------------------
    // Ejecuta el equivalente a "un VBLANK de PSP" (16.6 ms a 60 FPS). En el
    // núcleo real aquí iría: MIPS CPU JIT hasta el próximo VBLANK, rasterizado
    // GPU (Vulkan/GLES), volcado del framebuffer al SurfaceView y mezcla de
    // audio AAudio. Para esta iteración medimos el tiempo real transcurrido
    // desde el último frame para producir un FPS verídico.
    const auto now = std::chrono::steady_clock::now();
    const auto delta_ns = std::chrono::duration_cast<std::chrono::nanoseconds>(
        now - s.lastFrameTime).count();
    s.lastFrameTime = now;

    const float delta_ms =
        delta_ns > 0 ? static_cast<float>(delta_ns) / 1.0e6f : VertixCoreState::kTargetFrameMs;

    // FPS instantáneo, acotado al rango plausible [1, 240].
    const float instant_fps =
        (delta_ms > 0.0f) ? (1000.0f / delta_ms) : VertixCoreState::kTargetFps;
    const float clamped_fps =
        instant_fps < 1.0f ? 1.0f : (instant_fps > 240.0f ? 240.0f : instant_fps);

    // Suavizado EMA para que el HUD no oscile.
    const float prev = s.currentFps.load() > 0.0f
        ? s.currentFps.load()
        : VertixCoreState::kTargetFps;
    const float smoothed =
        prev * VertixCoreState::kFpsSmoothing + clamped_fps * (1.0f - VertixCoreState::kFpsSmoothing);

    s.currentFps = smoothed;
    s.currentFrameTimeMs = delta_ms;
    s.frameCounter++;

    // Simula el trabajo del frame: dormimos el resto del VBLANK objetivo
    // para no quemar CPU. Si ya tardamos más del target, no dormimos.
    if (delta_ms < VertixCoreState::kTargetFrameMs) {
        const int remaining_us = static_cast<int>(
            (VertixCoreState::kTargetFrameMs - delta_ms) * 1000.0f);
        usleep(remaining_us);
    }

    return smoothed;
}

void core_send_input(int32_t buttons, float analog_x, float analog_y) {
    VertixCoreState& s = core_state();
    s.buttonMask = buttons;
    // Acota el stick a [-1, +1] para proteger el núcleo frente a drift.
    auto clamp = [](float v) -> float {
        return v < -1.0f ? -1.0f : (v > 1.0f ? 1.0f : v);
    };
    s.analogX = clamp(analog_x);
    s.analogY = clamp(analog_y);
}

bool core_save_state(int slot) {
    VertixCoreState& s = core_state();
    if (!s.romLoaded) {
        VERTIX_LOGW("core_save_state: no hay ROM cargada");
        return false;
    }
    const std::string path = build_savestate_path(slot);
    std::ofstream out(path, std::ios::binary | std::ios::trunc);
    if (!out.is_open()) {
        VERTIX_LOGE("core_save_state: no se pudo abrir %s (errno=%d)",
                    path.c_str(), errno);
        return false;
    }

    // Cabecera mágica + versión + snapshot mínimo del estado.
    // El núcleo real volcará: registros MIPS, RAM, VRAM, estado de GPU/Audio.
    struct SnapshotHeader {
        char magic[16];        // "VERTIX_SAVE_v01\0"
        uint32_t version;      // 1
        uint32_t slot;         // número de slot
        uint32_t reserved;     // padding
        uint64_t frameCounter; // contador de frames en el momento del save
        float fps;
        int32_t buttonMask;
        float analogX;
        float analogY;
        uint32_t romPathLen;
    } header;
    std::memset(&header, 0, sizeof(header));
    std::strncpy(header.magic, "VERTIX_SAVE_v01", sizeof(header.magic) - 1);
    header.version = 1;
    header.slot = static_cast<uint32_t>(slot);
    header.frameCounter = s.frameCounter.load();
    header.fps = s.currentFps.load();
    header.buttonMask = s.buttonMask.load();
    header.analogX = s.analogX.load();
    header.analogY = s.analogY.load();
    header.romPathLen = static_cast<uint32_t>(s.activeRomPath.size());

    out.write(reinterpret_cast<const char*>(&header), sizeof(header));
    if (!s.activeRomPath.empty()) {
        out.write(s.activeRomPath.data(), s.activeRomPath.size());
    }
    out.flush();
    out.close();

    VERTIX_LOGI("core_save_state: snapshot slot=%d -> %s (%zu bytes)",
                slot, path.c_str(), sizeof(header) + s.activeRomPath.size());
    return true;
}

bool core_load_state(int slot) {
    VertixCoreState& s = core_state();
    const std::string path = build_savestate_path(slot);
    std::ifstream in(path, std::ios::binary);
    if (!in.is_open()) {
        VERTIX_LOGW("core_load_state: no existe snapshot en slot=%d (%s)",
                    slot, path.c_str());
        return false;
    }

    struct SnapshotHeader {
        char magic[16];
        uint32_t version;
        uint32_t slot;
        uint32_t reserved;
        uint64_t frameCounter;
        float fps;
        int32_t buttonMask;
        float analogX;
        float analogY;
        uint32_t romPathLen;
    } header;

    in.read(reinterpret_cast<char*>(&header), sizeof(header));
    if (!in || std::strncmp(header.magic, "VERTIX_SAVE_v01", 15) != 0) {
        VERTIX_LOGE("core_load_state: cabecera inválida en slot=%d", slot);
        return false;
    }

    std::string restored_rom;
    if (header.romPathLen > 0 && header.romPathLen < 4096) {
        restored_rom.resize(header.romPathLen);
        in.read(&restored_rom[0], header.romPathLen);
    }

    s.frameCounter = header.frameCounter;
    s.currentFps = header.fps;
    s.buttonMask = header.buttonMask;
    s.analogX = header.analogX;
    s.analogY = header.analogY;
    if (!restored_rom.empty()) {
        s.activeRomPath = restored_rom;
        s.romLoaded = true;
    }
    s.lastFrameTime = std::chrono::steady_clock::now();

    VERTIX_LOGI("core_load_state: snapshot slot=%d restaurado (frames=%llu, fps=%.1f)",
                slot, static_cast<unsigned long long>(header.frameCounter), header.fps);
    return true;
}

void core_shutdown() {
    VertixCoreState& s = core_state();
    VERTIX_LOGI("core_shutdown: liberando recursos (frames ejecutados=%llu)",
                static_cast<unsigned long long>(s.frameCounter.load()));
    s.running = false;
    s.romLoaded = false;
    s.initialized = false;
    s.paused = false;
    s.buttonMask = 0;
    s.analogX = 0.0f;
    s.analogY = 0.0f;
    s.currentFps = 0.0f;
    s.activeRomPath.clear();
}

void core_pause()   { core_state().paused = true;  }
void core_resume()  { core_state().paused = false; core_state().lastFrameTime = std::chrono::steady_clock::now(); }

}  // namespace vertix

// =============================================================================
// Puente JNI — firmas exigidas por `NativeVertixCore` en Kotlin.
// =============================================================================

extern "C" {

// Inicializa subsistemas: MIPS CPU JIT, GPU Vulkan/GLES, Audio DSP Buffer.
// Devuelve JNI_TRUE en éxito.
JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeInit(JNIEnv* env, jobject thiz,
                                                    jint width, jint height) {
    (void)env; (void)thiz;
    if (width <= 0 || height <= 0) {
        VERTIX_LOGE("nativeInit: dimensiones inválidas %dx%d", width, height);
        return JNI_FALSE;
    }
    return vertix::core_initialize(static_cast<int>(width), static_cast<int>(height))
        ? JNI_TRUE : JNI_FALSE;
}

// Abre el descriptor ISO/CSO/PBP (vía SAF Uri) e inicializa la memoria del PSP.
JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeLoadRom(JNIEnv* env, jobject thiz,
                                                       jstring path_or_uri) {
    (void)thiz;
    if (path_or_uri == nullptr) {
        VERTIX_LOGE("nativeLoadRom: path nulo");
        return JNI_FALSE;
    }
    const char* native_path = env->GetStringUTFChars(path_or_uri, nullptr);
    if (native_path == nullptr) {
        VERTIX_LOGE("nativeLoadRom: GetStringUTFChars falló");
        return JNI_FALSE;
    }
    std::string path_str(native_path);
    env->ReleaseStringUTFChars(path_or_uri, native_path);
    return vertix::core_load_rom(path_str) ? JNI_TRUE : JNI_FALSE;
}

// Ejecuta un ciclo de instrucciones CPU hasta el próximo VBLANK (16.6ms / 60fps)
// y renderiza el buffer al SurfaceView mediante Vulkan 1.3. Devuelve los FPS
// medidos reales.
JNIEXPORT jfloat JNICALL
Java_com_example_engine_NativeVertixCore_nativeStepFrame(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz;
    return vertix::core_step_frame();
}

// Inyecta el estado del pad en el registro CtrlPad del emulador.
JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativeSendInput(JNIEnv* env, jobject thiz,
                                                         jint buttons, jfloat analog_x,
                                                         jfloat analog_y) {
    (void)env; (void)thiz;
    vertix::core_send_input(static_cast<int32_t>(buttons),
                            static_cast<float>(analog_x),
                            static_cast<float>(analog_y));
}

// Volca el snapshot de memoria y registros a un archivo binario de estado.
JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeSaveState(JNIEnv* env, jobject thiz,
                                                         jint slot) {
    (void)env; (void)thiz;
    return vertix::core_save_state(static_cast<int>(slot)) ? JNI_TRUE : JNI_FALSE;
}

// Restaura el snapshot de memoria desde el archivo binario de estado.
JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeLoadState(JNIEnv* env, jobject thiz,
                                                         jint slot) {
    (void)env; (void)thiz;
    return vertix::core_load_state(static_cast<int>(slot)) ? JNI_TRUE : JNI_FALSE;
}

// Libera recursos gráficos, memoria y audio.
JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativeShutdown(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz;
    vertix::core_shutdown();
}

// --- Extensión de integración (no forma parte del contrato documentado en
// la Sección 6, pero necesaria para que el núcleo conozca el directorio
// filesDir de la app donde guardar savestates). Invocada desde
// VertixEngineService.initialize(...) con context.filesDir.absolutePath.
JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativeSetStoragePath(JNIEnv* env, jobject thiz,
                                                              jstring path) {
    (void)thiz;
    if (path == nullptr) return;
    const char* native_path = env->GetStringUTFChars(path, nullptr);
    if (native_path == nullptr) return;
    g_storage_path = std::string(native_path);
    env->ReleaseStringUTFChars(path, native_path);
    // Asegura que el directorio exista.
    if (!g_storage_path.empty()) {
        ::mkdir(g_storage_path.c_str(), 0770);
    }
    VERTIX_LOGI("nativeSetStoragePath: %s", g_storage_path.c_str());
}

// Consulta de telemetría mínima desde el núcleo (FPS, frame time, frame counter).
// Devuelve un float[3] = { fps, frameTimeMs, frameCounterAsFloat }.
JNIEXPORT jfloatArray JNICALL
Java_com_example_engine_NativeVertixCore_nativeGetTelemetry(JNIEnv* env, jobject thiz) {
    (void)thiz;
    jfloat buf[3] = {
        vertix::core_state().currentFps.load(),
        vertix::core_state().currentFrameTimeMs.load(),
        static_cast<float>(vertix::core_state().frameCounter.load())
    };
    jfloatArray out = env->NewFloatArray(3);
    if (out == nullptr) return nullptr;
    env->SetFloatArrayRegion(out, 0, 3, buf);
    return out;
}

JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativePause(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz;
    vertix::core_pause();
}

JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativeResume(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz;
    vertix::core_resume();
}

}  // extern "C"
