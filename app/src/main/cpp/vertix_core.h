// =============================================================================
// VERTIX PSP Emulator — Native Core Header
// -----------------------------------------------------------------------------
// Contrato C++17 expuesto por `libvertix_core.so` y consumido por la capa
// Kotlin a través de JNI (ver `NativeVertixCore` en
// `engine/VertixEmulatorEngine.kt`).
//
// Este archivo define:
//   * Las máscaras de botones PSP compatibles con el registro CtrlPad real.
//   * El estado interno del núcleo de emulación (VertixCoreState).
//   * La API C pura que el puente JNI invoca (vertix_core_*).
//
// La implementación actual es un "scaffolding" funcional: ejecuta un bucle de
// frame con temporización real (std::chrono) para producir mediciones de FPS
// verídicas, persiste savestates a disco y enruta la entrada a un registro
// de mandos. Está diseñada para ser sustituida incrementalmente por el
// núcleo de emulación MIPS (PPSSPP / Libretro) sin cambiar las firmas JNI.
// =============================================================================

#ifndef VERTIX_CORE_H
#define VERTIX_CORE_H

#include <cstdint>
#include <atomic>
#include <chrono>
#include <string>

namespace vertix {

// ---------------------------------------------------------------------------
// PSP Controller button masks (compatible con PSP_CTRL_* de libpspctrl).
// Estos valores son los que `nativeSendInput(buttons, analogX, analogY)`
// recibe desde Kotlin.
// ---------------------------------------------------------------------------
namespace psp_button {
    constexpr int32_t TRIANGLE  = 0x001000;  // △
    constexpr int32_t CIRCLE    = 0x002000;  // ○
    constexpr int32_t CROSS     = 0x004000;  // ✕
    constexpr int32_t SQUARE    = 0x008000;  // □
    constexpr int32_t DPAD_UP    = 0x000010;
    constexpr int32_t DPAD_DOWN  = 0x000040;
    constexpr int32_t DPAD_LEFT  = 0x000080;
    constexpr int32_t DPAD_RIGHT = 0x000020;
    constexpr int32_t L_TRIGGER  = 0x000100;
    constexpr int32_t R_TRIGGER  = 0x000200;
    constexpr int32_t START      = 0x000800;
    constexpr int32_t SELECT     = 0x000001;
    constexpr int32_t HOME       = 0x01000000;
    constexpr int32_t HOLD       = 0x02000000;
    constexpr int32_t ANALOG     = 0x800000;  // Marca: el stick analógico está activo
}

// ---------------------------------------------------------------------------
// Resoluciones internas de renderizado soportadas (ver SettingsScreen.kt).
// El núcleo escala el framebuffer nativo de la PSP (480x272) por este factor.
// ---------------------------------------------------------------------------
namespace render_scale {
    constexpr int NATIVE_1X = 1;   // 480x272
    constexpr int HD_2X     = 2;   // 960x544
    constexpr int FHD_3X    = 3;   // 1440x816
    constexpr int UHD_4X    = 4;   // 1920x1088
}

// ---------------------------------------------------------------------------
// Estado interno del núcleo de emulación.
//
// Se mantiene deliberadamente compacto: el objetivo de esta iteración es
// disponer de un backend C++ enlazable y verificable, no un emulador MIPS
// completo. Cuando se integre el núcleo real (PPSSPP / Libretro), este struct
// se reemplazará por el contexto de emulación verdadero.
// ---------------------------------------------------------------------------
struct VertixCoreState {
    std::atomic<bool> initialized{false};
    std::atomic<bool> romLoaded{false};
    std::atomic<bool> running{false};
    std::atomic<bool> paused{false};

    int32_t surfaceWidth{0};
    int32_t surfaceHeight{0};
    int32_t renderScale{render_scale::HD_2X};

    // Registro de mandos PSP (bits = psp_button::*).
    std::atomic<int32_t> buttonMask{0};
    // Stick analógico normalizado [-1.0, +1.0] en X e Y.
    std::atomic<float> analogX{0.0f};
    std::atomic<float> analogY{0.0f};

    // Telemetría de frame — actualizada por stepFrame().
    std::chrono::steady_clock::time_point lastFrameTime{};
    std::atomic<float> currentFps{0.0f};
    std::atomic<float> currentFrameTimeMs{0.0f};
    std::atomic<uint64_t> frameCounter{0};

    // Ruta del ROM actualmente cargado (para logs / resumen).
    std::string activeRomPath;

    // FPS suavizado con EMA para estabilizar la lectura del HUD.
    static constexpr float kFpsSmoothing = 0.9f;
    static constexpr float kTargetFps    = 60.0f;       // PSP VBLANK
    static constexpr float kTargetFrameMs = 1000.0f / kTargetFps;
};

// ---------------------------------------------------------------------------
// API C pura invocada desde el puente JNI. Definida en `vertix_core_jni.cpp`.
// ---------------------------------------------------------------------------
bool core_initialize(int width, int height);
bool core_load_rom(const std::string& path_or_uri);
float core_step_frame();
void core_send_input(int32_t buttons, float analog_x, float analog_y);
bool core_save_state(int slot);
bool core_load_state(int slot);
void core_shutdown();
void core_pause();
void core_resume();

// Acceso al estado global (singleton con duración igual al proceso).
VertixCoreState& core_state();

}  // namespace vertix

#endif  // VERTIX_CORE_H
