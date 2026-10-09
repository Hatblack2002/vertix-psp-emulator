# VERTIX — PSP Emulator for Android

<p align="center">
  <img src="app/src/main/res/drawable/ic_vertix_logo.jpg" alt="VERTIX Logo" width="128" style="border-radius: 24px;" />
</p>

<p align="center">
  <strong>Tu biblioteca PSP, en la palma de tu mano.</strong><br>
  Emulador de PlayStation Portable de alto rendimiento para Android con interfaz moderna, controles táctiles personalizables y arquitectura nativa.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_API_24+-blue.svg" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_M3-00E5FF.svg" alt="Compose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM_%2B_JNI_Ready-7B3DFF.svg" alt="Architecture" />
  <img src="https://img.shields.io/badge/Core-C%2B%2B17_NDK_27-FF647C.svg" alt="Native Core" />
</p>

<p align="center">
  <a href="https://github.com/Hatblack2002/vertix-psp-emulator/actions/workflows/build-apk.yml">
    <img src="https://github.com/Hatblack2002/vertix-psp-emulator/actions/workflows/build-apk.yml/badge.svg" alt="Build APK" />
  </a>
</p>

---

## Características Principales

* **Identidad Visual Propia**: Sistema de diseño exclusivo sin plantillas genéricas. Paleta cromática oscura cinemática con tokens violeta (`#7B3DFF`) y cian (`#00E5FF`).
* **Arranque Profesional Limpio**: Abre sin catálogos falsos ni juegos precargados ficticios; permite importar tus propios archivos legítimos `.iso`, `.cso` y `.pbp`.
* **Importación Real desde Almacenamiento**: Integración completa con el **Storage Access Framework (SAF)** de Android para seleccionar archivos directamente de la memoria del teléfono y persistirlos localmente.
* **Biblioteca con Vista Dual**: Conmutador fluido entre vista de cuadrícula (proporción 3:4 con portadas de alta definición) y vista de lista con identificadores técnicos y tamaños.
* **Emulación Real — Núcleo PPSSPP v1.19.3**: los juegos `.iso`, `.cso` y `.pbp` se ejecutan de verdad (CPU MIPS con JIT, GPU OpenGL ES/Vulkan, audio OpenSL ES).
* **Controles estilo PSP en el juego**: el propio núcleo superpone stick analógico, cruceta, △ ○ ✕ □, gatillos y Start/Select sobre la pantalla en horizontal (modo película), con soporte de mandos Bluetooth.
* **Portadas reales**: el icono de cada juego (ICON0.PNG) se extrae directamente del archivo ISO/PBP — sin imágenes inventadas.
  * Gatillos superiores L y R.
  * Menú rápido en juego para guardar y cargar partidas (*Savestates*).
* **Ajustes de Renderizado y Audio**:
  * Resoluciones internas de 1x (PSP nativo) a 4x (4K).
  * Backend gráfico (Vulkan 1.3 / OpenGL ES 3.2).
  * Control de opacidad y escala de botones táctiles en tiempo real.
  * Ajustes de búfer de audio de baja latencia con AAudio.
* **Telemetría de Rendimiento**:
  * Monitorización en tiempo real de FPS, tiempo de fotograma, uso de memoria JVM y núcleos del procesador.
  * Gráfica fluida de fotogramas dibujada en Canvas.
  * Visor de registros de depuración categorizados por subsistema.

---

## Estructura del Proyecto

```
VERTIX/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt             # Punto de entrada, SAF picker y navegación
│   │   │   ├── engine/
│   │   │   │   └── VertixEmulatorEngine.kt # Contrato JNI para el núcleo C++
│   │   │   ├── model/
│   │   │   │   └── VertixModels.kt         # Modelos de datos (PspGame, Settings, etc.)
│   │   │   ├── storage/
│   │   │   │   └── GameRepository.kt       # Persistencia local JSON de juegos importados
│   │   │   ├── ui/
│   │   │   │   ├── components/             # Botones, tarjetas y logotipo VERTIX
│   │   │   │   ├── screens/                # Todas las pantallas del emulador
│   │   │   │   └── theme/                  # Tokens de color, tipografía y tema
│   │   │   └── viewmodel/
│   │   │       └── VertixViewModel.kt      # ViewModel reactivo con StateFlow
│   │   └── res/
│   │       ├── font/                       # Fuentes locales Orbitron e Inter
│   │       └── drawable/                   # Arte de carátulas e icono adaptativo
├── DOCUMENTACION.md                        # Documentación técnica exhaustiva
└── build.gradle.kts                        # Configuración de compilación Gradle
```

---

## Compilación y Ejecución

Para compilar el proyecto en Android Studio o por terminal:

```bash
# Compilar APK en modo depuración
gradle :app:assembleDebug

# Ejecutar tests
gradle :app:testDebugUnitTest
```

El APK compilado se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Integración con el Motor C++ (PPSSPP Core)

El motor de emulación es el núcleo C++ real de **PPSSPP v1.19.3** (`libppsspp_jni.so`), compilado desde la fuente oficial en la CI y enlazado mediante el glue Java oficial (`org.ppsspp.ppsspp`). Consulta [DOCUMENTACION.md](DOCUMENTACION.md) para la arquitectura completa y la guía de compilación local.

### Integración del PPSSPP Core real

Para reemplazar el núcleo de scaffolding por el motor de emulación PPSSPP real:

```bash
# 1. Clona y compila el core PPSSPP para tu ABI (≈30 min)
scripts/prepare-ppsspp.sh v1.18.1 arm64-v8a

# 2. Compila VERTIX con el flag VERTIX_LINK_PPSSPP=ON
./gradlew :app:assembleDebug \
  -PcmakeArgs="-DVERTIX_LINK_PPSSPP=ON"
```

Alternativamente, ejecuta el workflow `Integrate PPSSPP Core` desde la pestaña **Actions** del repo en GitHub:

1. Ve a **Actions → Integrate PPSSPP Core → Run workflow**.
2. Indica el ref (`master`, `v1.18.1`, etc.) y las ABIs.
3. Espera ~60-90 minutos.
4. Descarga el artefacto `vertix-core-ppsspp-linked`.
5. Copia los `.so` a `app/src/main/jniLibs/<abi>/`.

Para más detalle, consulta [`.github/SECRETS_SETUP.md`](.github/SECRETS_SETUP.md) y la documentación técnica [DOCUMENTACION.md](DOCUMENTACION.md).

---

## Continuous Integration

El proyecto incluye dos workflows de GitHub Actions:

| Workflow | Disparador | Propósito |
|---|---|---|
| [`build-apk.yml`](.github/workflows/build-apk.yml) | `push` a main/develop, `pull_request`, manual | **Pipeline real**: clona PPSSPP v1.19.3 con submódulos, compila `libppsspp_jni.so` con ndk-build (arm64-v8a + armeabi-v7a), empaqueta el APK con el núcleo, ejecuta tests unitarios y publica el artefacto `vertix-apk-debug`. |

### Protección de rama `main`

Recomendado: configurar **Branch Protection** en `Settings → Branches`:

- ✅ Require status checks: `Build APK (debug)`
- ✅ Require branches up to date before merging
- ✅ Require conversation resolution

Instrucciones detalladas en [`.github/SECRETS_SETUP.md`](.github/SECRETS_SETUP.md).

---

## Licencia

Proyecto VERTIX — Creado para desarrollo de software y emulación de videojuegos en Android.
