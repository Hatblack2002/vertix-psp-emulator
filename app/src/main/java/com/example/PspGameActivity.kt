package com.example

import org.ppsspp.ppsspp.PpssppActivity

/**
 * Pantalla REAL de emulación PSP.
 *
 * Hereda de org.ppsspp.ppsspp.PpssppActivity, que:
 *  1. Carga la librería nativa del núcleo de emulación `libppsspp_jni.so`
 *     (PPSSPP v1.19.3 compilado desde fuente).
 *  2. Lee la ruta del juego desde el intent (data URI o extra
 *     "org.ppsspp.ppsspp.Shortcuts") y arranca el juego en el núcleo C++.
 *  3. Gestiona el render (GL/Vulkan), el audio (OpenSL ES), los controles
 *     táctiles tipo PSP sobre el juego, la rotación de pantalla según el
 *     juego y el menú nativo del emulador (botón atrás).
 *
 * Aquí NO hay nada simulado: si el ISO/CSO/PBP existe, se ejecuta de verdad.
 */
class PspGameActivity : PpssppActivity()
