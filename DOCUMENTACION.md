# VERTIX — Emulador de PSP para Android
## Documentación Técnica y Arquitectura del Sistema

**Versión del documento:** 1.0  
**Proyecto:** VERTIX PSP Emulator  
**Plataforma objetivo:** Android (API 24+)  
**Tecnología:** Kotlin, Jetpack Compose, Material Design 3, NDK/JNI ready  
**Licencia:** Propietaria / Código abierto para desarrollo de emulación  

---

## 1. Visión del Proyecto y Filosofía de Diseño

El proyecto **VERTIX** es un emulador de PlayStation Portable (PSP) para Android concebido con una dirección artística moderna, técnica y cinematográfica. A diferencia de las aplicaciones genéricas o paneles adaptados, VERTIX implementa una identidad visual propia basada en:

1. **Diseño prioritario para dispositivos móviles Android**: Adaptable a orientación vertical (navegación y gestión de biblioteca) y orientación horizontal (ejecución de juegos y controles en pantalla).
2. **Apertura profesional vacía**: La aplicación inicia de forma limpia y profesional sin catálogos ficticios predefinidos; invita al usuario a importar sus copias de seguridad de juegos legítimas (`.iso`, `.cso`, `.pbp`) directamente desde el almacenamiento del dispositivo.
3. **Controles táctiles de alta precisión**: Cruceta direccional (D-Pad) con respuesta vectorial luminosa, stick analógico virtual con arrastre y recentrado, botones de acción en rombo (△, ○, ✕, □) y gatillos superiores L y R con respuesta háptica.
4. **Telemetría real del hardware**: Monitorización de memoria JVM, procesadores lógicos disponibles y medición del intervalo de fotogramas sin inventar cifras arbitrarias.

---

## 2. Sistema de Tokens de Diseño

La paleta cromática y la tipografía están centralizadas en `ui/theme/`:

| Token | Código HEX | Uso en el Sistema |
|---|---|---|
| **Fondo principal** | `#080F16` | Lienzo general y fondo de la aplicación |
| **Superficie principal** | `#101A27` | Paneles, cabeceras y tarjetas elevadas |
| **Superficie secundaria** | `#162334` | Contenedores secundarios y menús contextuales |
| **Superficie terciaria** | `#1D2C40` | Elementos seleccionados y píldoras activas |
| **Violeta principal** | `#7B3DFF` | Identidad de marca, botón primario y foco |
| **Cian secundario** | `#00E5FF` | Detalles técnicos, indicadores de rendimiento y acentos |
| **Texto principal** | `#F0F4FC` | Títulos, textos primarios y etiquetas de acción |
| **Texto secundario** | `#9CAEC4` | Descripciones, metadatos y rutas |
| **Borde normal** | `#26384F` | Líneas de división y contornos sutiles |
| **Éxito / Compatible** | `#35D6A0` | Estado óptimo, juegos verificados |
| **Advertencia** | `#FFBE63` | Avisos de rendimiento o compatibilidad media |
| **Error** | `#FF647C` | Fallos de lectura y acciones destructivas |

### Tipografía
- **Orbitron** (`res/font/orbitron.ttf`): Utilizada en el logotipo, títulos de pantalla, encabezados de sección y en el overlay HUD. Aporta la personalidad estética de consola moderna.
- **Inter** (`res/font/inter.ttf`): Empleada en todo el cuerpo de texto, botones, listas, controles de ajustes y metadatos para garantizar legibilidad y accesibilidad.
- **Monospace**: Aplicada a rutas de archivos, identificadores de juego (ej. `ULUS10041`), registros de depuración y tamaños en disco.

---

## 3. Arquitectura del Código Android

La estructura del proyecto sigue el patrón **MVVM (Model-View-ViewModel)** con flujo unidireccional de datos (`StateFlow`):

```
app/src/main/java/com/example/
├── MainActivity.kt                 # Actividad principal, insets de pantalla, SAF launcher y navegación
├── engine/
│   └── VertixEmulatorEngine.kt     # Contrato del motor nativo, JNI bindings y telemetría de hardware
├── model/
│   └── VertixModels.kt             # Modelos de datos inmutables (PspGame, SaveState, Settings, Telemetry)
├── storage/
│   └── GameRepository.kt           # Persistencia local JSON en almacenamiento interno e importación SAF
├── ui/
│   ├── components/
│   │   └── VertixComponents.kt     # Componentes reutilizables M3 (Botones, Logotipo, Badges, Diálogos)
│   ├── screens/
│   │   ├── SplashScreen.kt         # Pantalla de arranque cinemática con barra de carga
│   │   ├── HomeScreen.kt           # Panel principal con tarjeta de continuidad y accesos
│   │   ├── LibraryScreen.kt        # Biblioteca con filtros, buscador y vistas cuadrícula/lista
│   │   ├── GameDetailScreen.kt     # Ficha técnica, portadas, metadatos y partidas guardadas
│   │   ├── GameRunningScreen.kt    # Pantalla horizontal de juego a 60 FPS con controles táctiles y menú rápido
│   │   ├── SettingsScreen.kt       # Ajustes de renderizado (Vulkan), audio y sensibilidad táctil
│   │   ├── FilesScreen.kt          # Explorador de archivos con botón de importación SAF
│   │   ├── PerformanceScreen.kt    # Gráfica Canvas en tiempo real, CPU, RAM y visor de registros
│   │   └── DesignSystemScreen.kt   # Showcase maestro del sistema de diseño y sus 20 entregables
│   └── theme/
│       ├── Color.kt                # Definición de tokens cromáticos
│       ├── Theme.kt                # Tema oscuro cinematográfico M3
│       └── Type.kt                 # Jerarquía tipográfica Orbitron + Inter
└── viewmodel/
    └── VertixViewModel.kt          # Gestión de estado reactivo, eventos e interacción con el motor
```

---

## 4. Importación Real desde el Almacenamiento

Se ha implementado el **Storage Access Framework (SAF)** nativo de Android:

1. **Selector de Documentos del Sistema**: En `MainActivity.kt`, se registra un launcher `ActivityResultContracts.OpenMultipleDocuments()`.
2. **Lectura de Metadatos Reales**: En `GameRepository.kt`, se analiza el `Uri` obtenido mediante el `ContentResolver`, consultando:
   - `OpenableColumns.DISPLAY_NAME`: Nombre del archivo en disco.
   - `OpenableColumns.SIZE`: Tamaño exacto en bytes, formateado dinámicamente en MB o GB.
3. **Detección de Formato**: Extrae la extensión (`.ISO`, `.CSO`, `.PBP`) y limpia el título para una visualización óptima en la tarjeta.
4. **Persistencia Local**: Los juegos importados se guardan en formato JSON en el directorio privado de la aplicación (`filesDir/vertix_library.json`). Permanecen guardados entre ejecuciones sin requerir re-escaneo constante.
5. **Eliminación Segura**: Eliminar un juego de la biblioteca retira el registro de la aplicación sin borrar el archivo original del almacenamiento del usuario.

---

## 5. Motor de Emulación REAL — PPSSPP v1.19.3

VERTIX ya **no contiene ninguna simulación**: la emulación la realiza el núcleo
C++ de [PPSSPP](https://www.ppsspp.org) v1.19.3 (GPL v2+), compilado desde la
fuente oficial dentro de la CI.

### 5.1 Arquitectura del núcleo real

| Pieza | Descripción |
|---|---|
| `libppsspp_jni.so` | Núcleo nativo real (CPU MIPS R4000 con JIT, GPU GL/Vulkan, audio OpenSL ES, UECD). Se compila con `ndk-build` desde el código oficial de PPSSPP y se empaqueta en `app/src/main/jniLibs/<abi>/`. |
| `org.ppsspp.ppsspp.*` (glue Java) | Clases puente oficiales de PPSSPP (`NativeActivity`, `PpssppActivity`, `NativeApp`, vistas GL/Vulkan, audio, input). Viven en ese paquete porque el binario C++ exporta JNI con ese namespace. |
| `com.example.PspGameActivity` | Actividad de juego de VERTIX: extiende `PpssppActivity`, se declara en landscape y arranca el juego cuya ruta llega por intent. |
| `engine/PspLauncher.kt` | Lanzador: valida que la copia local exista y envía la ruta absoluta al núcleo. Si el archivo no existe devuelve `false` (nunca simula un arranque). |
| `engine/PspArtwork.kt` | Extrae la portada **real** (ICON0.PNG) del propio archivo del juego: recorre el sistema de archivos ISO9660 (PVD sector 16 → PSP_GAME/ICON0.PNG) o el header PBP. En CSO/CHD devuelve null y la UI muestra un marcador neutral. |
| `storage/GameRepository.kt` | Importa copiando el juego completo a `filesDir/games/` (los URI de SAF expiran), verifica la cabecera (ISO/PBP/CSO/CHD) y guarda la portada extraída en `filesDir/covers/`. |

### 5.2 Flujo de arranque de un juego (100% real)

1. El usuario importa un `.iso/.cso/.pbp` con el selector SAF del sistema.
2. `GameRepository` copia el archivo a almacenamiento privado y extrae `ICON0.PNG` del ISO/PBP.
3. Al pulsar "Jugar", `VertixViewModel.launchGame()` garantiza la copia local y llama a `PspLauncher`.
4. `PspLauncher` inicia `PspGameActivity` (landscape, fullscreen) con la ruta del juego.
5. `PpssppActivity` carga `libppsspp_jni.so` y pasa la ruta al núcleo C++, que emula el juego real:
   controles táctiles estilo PSP sobre el juego, rotación según el juego (horizontal), menú nativo
   del emulador con el botón atrás (gráficos, audio, savestates, FPS reales en pantalla).

### 5.3 Lo que ya no existe (auditoría anti-simulación)

| Eliminado | Motivo |
|---|---|
| `engine/VertixEmulatorEngine.kt` (NativeVertixCore, VertixEngineService, modo simulado) | Núcleo falso que dormía el hilo y calculaba FPS inventados. |
| `app/src/main/cpp/` (vertix_core_jni.cpp, CMakeLists) | "Emulador" de mentira: `core_load_rom` solo guardaba la ruta. |
| `ui/screens/GameRunningScreen.kt` | Pantalla con imagen fija y controles decorativos que no afectaban a ningún juego. |
| Telemetría fabricada (`PerformanceMetrics.fps/cpuUsage/temperatura`, curva de FPS) | Los FPS reales los mide y muestra el propio PPSSPP (HUD nativo). |
| Portadas fijas (cover_gow / cover_gta / cover_tekken) | Ahora se extrae la portada real del archivo; si no existe, marcador neutral. |
| Logs falsos de arranque ("Vulkan 1.3", "ARM64 Core initialized") | El registro solo contiene eventos reales de importación/arranque/errores. |

---

## 6. CI — Compilación del núcleo real y del APK

El workflow `.github/workflows/build-apk.yml` hace, en dos jobs:

1. **build-native** (por ABI: `arm64-v8a`, `armeabi-v7a`):
   - Clona `https://github.com/hrydgard/ppsspp` en el tag `v1.19.3` con submódulos
     (`ffmpeg` con binarios precompilados por ABI, `glslang`, `SPIRV-Cross`, `armips`,
     `miniupnp`, `zstd`, `lua`...).
   - Compila con `ndk-build` el módulo `ppsspp_jni` → `libppsspp_jni.so`.
   - Verifica con `llvm-nm` que el binario exporta el puente JNI `Java_org_ppsspp_ppsspp_*`.
2. **build-apk**:
   - Coloca los `.so` en `app/src/main/jniLibs/<abi>/`.
   - `./gradlew :app:assembleDebug :app:testDebugUnitTest` (Java 21: Robolectric SDK 36 lo exige).
   - Verifica que el APK contiene `libppsspp_jni.so` para ambas ABIs y los assets de PPSSPP.
   - Publica el artefacto **vertix-apk-debug** listo para instalar.

### Requisitos de compilación local

```bash
# 1. Compilar el núcleo (requiere NDK r27)
git clone --depth 1 --branch v1.19.3 --recurse-submodules --shallow-submodules https://github.com/hrydgard/ppsspp.git
cd ppsspp/android && $ANDROID_NDK_HOME/ndk-build -j$(nproc) \
  NDK_PROJECT_PATH=. NDK_APPLICATION_MK=jni/Application.mk APP_BUILD_SCRIPT=jni/Android.mk \
  APP_ABI="arm64-v8a armeabi-v7a" ppsspp_jni
# -> libs/<abi>/libppsspp_jni.so

# 2. Copiar las librerías al proyecto
mkdir -p ../app/src/main/jniLibs && cp -r libs/* ../app/src/main/jniLibs/

# 3. Compilar el APK
./gradlew :app:assembleDebug
```

---

## 7. Licencias y créditos

- Núcleo de emulación: **PPSSPP** © Henrik Rydgård y contribuidores — GPL v2+.
  https://github.com/hrydgard/ppsspp — VERTIX incluye y usa el núcleo tal cual,
  compilado desde la fuente oficial; el glue Java (`org.ppsspp.ppsspp`) conserva
  su licencia original.
- Assets del emulador (fuentes flash0, shaders, idiomas, atles UI): incluidos tal
  cual del repositorio oficial de PPSSPP.
- Interfaz VERTIX (Compose): diseño propio del proyecto.
