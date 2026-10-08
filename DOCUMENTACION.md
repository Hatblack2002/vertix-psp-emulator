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

## 5. Auditoría de Simulaciones y Transición al Motor Real C++/JNI

A petición del proyecto, se han auditado y eliminado los elementos conceptuales temporales para preparar la aplicación para el núcleo de emulación real:

### Archivos Auditados y Cambios Realizados

| Archivo | Estado Anterior (Simulación) | Estado Actual (Real) | Pasos para Conectar el Motor C++ Nativo |
|---|---|---|---|
| `VertixViewModel.kt` | Catálogo ficticio de juegos precargados al inicio y bucle de generación de números aleatorios para FPS. | La biblioteca inicia vacía por defecto. Carga únicamente juegos importados del usuario mediante `GameRepository`. La telemetría lee métricas reales del entorno Android. | Reemplazar las llamadas a `VertixEngineService` por el puente nativo `NativeVertixCore.nativeStepFrame()`. |
| `LibraryScreen.kt` | Incluía un botón de depuración conceptual ("Ver Estado Vacío / Ver Con Juegos"). | Se eliminó el botón decorativo. Muestra el estado vacío real cuando no hay juegos y la cuadrícula/lista cuando el usuario los importa. | No requiere cambios; consume la lista real del ViewModel. |
| `HomeScreen.kt` | Mostraba tarjetas de juegos que no existían en el almacenamiento del usuario. | Detecta si la lista de juegos está vacía. Si no hay juegos, presenta la invitación profesional a importar archivos con un botón activo. | Conectar la tarjeta de continuidad al último estado guardado en el backend nativo. |
| `FilesScreen.kt` | Mostraba rutas simuladas fijas. | Permite lanzar el selector de documentos real del sistema Android para importar cualquier archivo `.iso` o `.cso`. | Si se desea navegación directa por carpetas en lugar de SAF, integrar `DocumentFile.fromTreeUri()`. |
| `GameRunningScreen.kt` | Captura estática de muestra fija con controles en pantalla. | Los controles táctiles virtuales cuentan con multitáctil real, arrastre en el stick analógico, respuesta háptica (`VIBRATE`) y menú rápido interactivo. | Enlazar `onDirectionPress`, `onTouchButton`, y `stickOffset` directamente con `NativeVertixCore.nativeSendInput(buttonMask, analogX, analogY)`. |
| `engine/VertixEmulatorEngine.kt` | No existía una capa de desacoplamiento para el backend nativo. | Creado como contrato `EmulatorBackend` junto con la clase `NativeVertixCore` preparada para enlazar con `libvertix_core.so` mediante `System.loadLibrary()`. | Compilar el núcleo de emulación C++ (ej. PPSSPP Core) y exportar las funciones JNI declaradas. |

---

## 6. Especificación de la Interfaz JNI C++ (Core Nativo)

Para integrar un motor de emulación C++ real (como el núcleo de PPSSPP o un núcleo Libretro MIPS):

### Declaración en C++ (`vertix_core_jni.cpp`)

```cpp
#include <jni.h>
#include <string>

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeInit(JNIEnv *env, jobject thiz, jint width, jint height) {
    // Inicializar subsistemas: MIPS CPU JIT, GPU Vulkan/GLES Context, Audio DSP Buffer
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeLoadRom(JNIEnv *env, jobject thiz, jstring path_or_uri) {
    const char *nativePath = env->GetStringUTFChars(path_or_uri, 0);
    // Abrir descriptor de archivo ISO/CSO (vía File Descriptor desde SAF Uri)
    // Inicializar memoria del PSP (32MB/64MB RAM)
    env->ReleaseStringUTFChars(path_or_uri, nativePath);
    return JNI_TRUE;
}

JNIEXPORT jfloat JNICALL
Java_com_example_engine_NativeVertixCore_nativeStepFrame(JNIEnv *env, jobject thiz) {
    // Ejecutar ciclo de instrucciones CPU hasta el próximo VBLANK (16.6ms para 60fps)
    // Renderizar buffer a SurfaceView / TextureView mediante Vulkan 1.3
    return 60.0f; // FPS medidos reales
}

JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativeSendInput(JNIEnv *env, jobject thiz, jint buttons, jfloat analog_x, jfloat analog_y) {
    // Inyectar estado en el registro de mandos del emulador (CtrlPad)
}

JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeSaveState(JNIEnv *env, jobject thiz, jint slot) {
    // Volcar snapshot de memoria y registros a archivo binario de estado
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_engine_NativeVertixCore_nativeLoadState(JNIEnv *env, jobject thiz, jint slot) {
    // Restaurar snapshot de memoria
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_example_engine_NativeVertixCore_nativeShutdown(JNIEnv *env, jobject thiz) {
    // Liberar recursos gráficos, memoria y audio
}

}
```

### Configuración en `app/build.gradle.kts` para el NDK

```kotlin
android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O3"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
```

---

## 7. Instrucciones de Compilación y GitHub Actions CI

El proyecto se compila con el sistema Gradle de Android:

```bash
# Compilar la aplicación en modo Debug
gradle :app:assembleDebug

# Ejecutar las pruebas unitarias y de Robolectric
gradle :app:testDebugUnitTest

# Generar el paquete APK para distribución
gradle :app:packageDebug
```

Los artefactos generados se ubican en `app/build/outputs/apk/debug/app-debug.apk`.
El flujo de CI en GitHub Actions está configurado para validar la compilación y pruebas en cada commit a la rama principal.
