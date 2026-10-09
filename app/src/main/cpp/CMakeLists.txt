# =============================================================================
# VERTIX PSP Emulator — CMake build script for libvertix_core.so
# -----------------------------------------------------------------------------
# Compila el núcleo nativo C++ que el objeto Kotlin `NativeVertixCore` carga vía
# `System.loadLibrary("vertix_core")`. La configuración sigue lo especificado
# en DOCUMENTACION.md (Sección 6).
#
# Salida: libvertix_core.so  → empaquetada en el APK bajo lib/<abi>/
# ABIs:   arm64-v8a, armeabi-v7a, x86_64  (definidos en app/build.gradle.kts)
# =============================================================================

cmake_minimum_required(VERSION 3.22.1)

project(vertix_core
    LANGUAGES CXX
    VERSION 1.0.0
    DESCRIPTION "VERTIX PSP Emulator — Native JNI core")

# ---------------------------------------------------------------------------
# Configuración del compilador.
# ---------------------------------------------------------------------------
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Banderas comunes: optimización agresiva + tratamiento de warnings como
# errores en cabeceras propias. Se omite -Werror en código de terceros.
add_compile_options(
    -Wall
    -Wextra
    -Wno-unused-parameter          # parámetros JNIEnv*/jobject thiz sin uso
    -ffunction-sections
    -fdata-sections
    -fvisibility=hidden            # solo los símbolos JNI exportan (default-hidden)
)

# Optimización por build type (O3 en Release, O0 + DEBUG en Debug).
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_compile_options(-O3 -DNDEBUG)
else()
    add_compile_options(-O0 -g -DVERTIX_DEBUG)
endif()

# ---------------------------------------------------------------------------
# Integración opcional del núcleo PPSSPP (libPPSSPPCore.a)
# ---------------------------------------------------------------------------
# Actívala con: cmake -DVERTIX_LINK_PPSSPP=ON ...
# Requiere que scripts/prepare-ppsspp.sh (o el workflow
# integrate-ppsspp-core.yml) haya generado prebuilt/<abi>/libPPSSPPCore.a.
# ---------------------------------------------------------------------------
option(VERTIX_LINK_PPSSPP "Enlazar libvertix_core.so contra el core PPSSPP" OFF)

set(PPSSPP_PREBUILT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/prebuilt/${ANDROID_ABI}")
if(VERTIX_LINK_PPSSPP)
    if(EXISTS "${PPSSPP_PREBUILT_DIR}/libPPSSPPCore.a")
        message(STATUS "VERTIX: enlazando contra PPSSPP Core (${PPSSPP_PREBUILT_DIR}/libPPSSPPCore.a)")
        set(VERTIX_HAS_PPSSPP TRUE)
    else()
        message(WARNING "VERTIX_LINK_PPSSPP=ON pero no se encontró "
                        "${PPSSPP_PREBUILT_DIR}/libPPSSPPCore.a — "
                        "compilando en modo scaffolding (sin núcleo MIPS real).")
        set(VERTIX_HAS_PPSSPP FALSE)
    endif()
else()
    set(VERTIX_HAS_PPSSPP FALSE)
endif()

# ---------------------------------------------------------------------------
# Biblioteca compartida principal: libvertix_core.so
# ---------------------------------------------------------------------------
add_library(vertix_core
    SHARED
    vertix_core_jni.cpp
)

# Cabeceras del proyecto (no compilan, pero los IDEs los indexan).
target_include_directories(vertix_core
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}
)

# ---------------------------------------------------------------------------
# Dependencias del sistema Android:
#   * log    → __android_log_print (logs visibles en adb logcat -s VertixNative)
#   * android → JNI_OnLoad helpers y APIs del NDK
#   * EGL / GLESv3 → backend gráfico OpenGL ES 3.2 (futuro, ya enlazado)
# ---------------------------------------------------------------------------
target_link_libraries(vertix_core
    PRIVATE
        log
        android
        EGL
        GLESv3
)

# ---------------------------------------------------------------------------
# Enlace condicional contra PPSSPP Core. Cuando VERTIX_HAS_PPSSPP es TRUE,
# la static library se añade al link line y se define VERTIX_HAS_PPSSPP
# como macro de preprocesador para que vertix_core_jni.cpp pueda activar
# las llamadas reales al núcleo MIPS (en lugar del scaffolding).
# ---------------------------------------------------------------------------
if(VERTIX_HAS_PPSSPP)
    target_link_libraries(vertix_core PRIVATE "${PPSSPP_PREBUILT_DIR}/libPPSSPPCore.a")
    target_compile_definitions(vertix_core PRIVATE VERTIX_HAS_PPSSPP=1)
    # Cabeceras públicas de PPSSPP (Core/, Common/, GPU/) para que el código
    # JNI pueda #include "Core/Core.h" etc.
    target_include_directories(vertix_core PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/../../third_party/ppsspp"
        "${CMAKE_CURRENT_SOURCE_DIR}/../../third_party/ppsspp/Common"
        "${CMAKE_CURRENT_SOURCE_DIR}/../../third_party/ppsspp/Core"
        "${CMAKE_CURRENT_SOURCE_DIR}/../../third_party/ppsspp/GPU"
    )
    # Las static libs de PPSSPP dependen de zlib, log, OpenSLES, EGL, GLESv3.
    # Aseguramos que estén al final del link line.
    target_link_libraries(vertix_core PRIVATE z OpenSLES EGL GLESv3)
else()
    target_compile_definitions(vertix_core PRIVATE VERTIX_HAS_PPSSPP=0)
endif()

# Reduce el tamaño del .so descartando secciones no referenciadas.
target_link_options(vertix_core
    PRIVATE
        -Wl,--gc-sections
        -Wl,--exclude-libs,ALL
        -Wl,-z,noexecstack
        -Wl,-z,relro
        -Wl,-z,now
)

# Nota sobre símbolos de debug:
#   En Debug, CMake NO pasa -s/--strip al linker, así que la tabla de símbolos
#   se conserva automáticamente. En Release, CMake añade -s automáticamente.
#   No añadimos -Wl,--no-strip porque lld (el linker del NDK) no lo soporta.
#   Para forzar conservar símbolos en Release, usar:
#     target_link_options(vertix_core PRIVATE -Wl,--export-dynamic)

# ---------------------------------------------------------------------------
# Mensaje informativo para el log de compilación.
# ---------------------------------------------------------------------------
message(STATUS "VERTIX: compilando libvertix_core.so para ABI=${ANDROID_ABI} "
               "build=${CMAKE_BUILD_TYPE} std=c++17")
