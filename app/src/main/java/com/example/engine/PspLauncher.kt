package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.PspGameActivity
import java.io.File

/**
 * Lanza un juego REAL en el núcleo PPSSPP (PspGameActivity).
 *
 * El núcleo acepta la ruta absoluta del archivo vía intent data
 * (org.ppsspp.ppsspp.PpssppActivity.parseIntent la envuelve y el C++ hace
 * el boot del juego). Devuelve false si el archivo no existe o el intent
 * no pudo iniciarse — nunca simula un arranque.
 */
object PspLauncher {

  private const val TAG = "VertixLaunch"

  fun launch(context: Context, localPath: String?): Boolean {
    if (localPath.isNullOrBlank()) return false
    val file = File(localPath)
    if (!file.exists() || file.length() == 0L) {
      Log.e(TAG, "Archivo de juego inexistente o vacío: $localPath")
      return false
    }
    return try {
      val intent = Intent(context, PspGameActivity::class.java).apply {
        data = Uri.parse(file.absolutePath)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      Log.i(TAG, "Boot enviado al núcleo PPSSPP: ${file.name} (${file.length() / (1024 * 1024)} MB)")
      true
    } catch (t: Throwable) {
      Log.e(TAG, "No se pudo iniciar PspGameActivity", t)
      false
    }
  }
}
