package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.engine.PspArtwork
import com.example.model.CompatibilityStatus
import com.example.model.PspGame
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Biblioteca de juegos REAL:
 *
 *  * Cada juego importado se COPIA a almacenamiento privado de la app
 *    (filesDir/games) para que el núcleo de emulación pueda leerlo siempre
 *    (los URIs de SAF expiran al reiniciar el dispositivo).
 *  * La portada se extrae del propio archivo del juego (ICON0.PNG del
 *    ISO9660 o del PBP). No se usan portadas falsas nunca.
 */
class GameRepository(private val context: Context) {
  private val storageFile = File(context.filesDir, "vertix_library.json")
  private val gamesDir = File(context.filesDir, "games").apply { mkdirs() }
  private val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

  fun getSavedGames(): List<PspGame> {
    if (!storageFile.exists()) return emptyList()
    return try {
      val jsonStr = storageFile.readText()
      val jsonArray = JSONArray(jsonStr)
      val list = mutableListOf<PspGame>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val coverPath = obj.optString("coverPath", "")
        list.add(
          PspGame(
            id = obj.optString("id", "GAME-$i"),
            title = obj.optString("title", "Juego desconocido"),
            subtitle = obj.optString("subtitle", "PSP Title"),
            genre = obj.optString("genre", "Acción"),
            rating = obj.optString("rating", "—"),
            format = obj.optString("format", "ISO"),
            size = obj.optString("size", "0 MB"),
            coverResId = obj.optInt("coverResId", 0),
            coverPath = coverPath.ifBlank { null },
            localPath = obj.optString("localPath", "").ifBlank { null },
            filePath = obj.optString("filePath", ""),
            status = try {
              CompatibilityStatus.valueOf(obj.optString("status", "PERFECT"))
            } catch (_: Exception) {
              CompatibilityStatus.UNVERIFIED
            },
            lastPlayed = obj.optString("lastPlayed", "Recién añadido"),
            playTime = obj.optString("playTime", "0m"),
            isFavorite = obj.optBoolean("isFavorite", false),
            description = obj.optString("description", "Juego importado desde el almacenamiento."),
            hasSavestate = obj.optBoolean("hasSavestate", false)
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveGames(games: List<PspGame>) {
    try {
      val jsonArray = JSONArray()
      for (game in games) {
        val obj = JSONObject().apply {
          put("id", game.id)
          put("title", game.title)
          put("subtitle", game.subtitle)
          put("genre", game.genre)
          put("rating", game.rating)
          put("format", game.format)
          put("size", game.size)
          put("coverResId", game.coverResId)
          put("coverPath", game.coverPath ?: "")
          put("localPath", game.localPath ?: "")
          put("filePath", game.filePath)
          put("status", game.status.name)
          put("lastPlayed", game.lastPlayed)
          put("playTime", game.playTime)
          put("isFavorite", game.isFavorite)
          put("description", game.description)
          put("hasSavestate", game.hasSavestate)
        }
        jsonArray.put(obj)
      }
      storageFile.writeText(jsonArray.toString())
    } catch (_: Exception) {
    }
  }

  /**
   * Importa un juego desde un Uri (SAF): copia el archivo a almacenamiento
   * privado, extrae la portada real y lo registra. Devuelve null si la
   * copia falla o el archivo no es una imagen de juego legible.
   */
  fun importFromUri(uri: Uri): PspGame? {
    var displayName = "PSP_Game.iso"
    var sizeBytes = 0L

    try {
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
          if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
          if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
        }
      }
    } catch (_: Exception) {
    }

    // Copia REAL del archivo a almacenamiento privado.
    val uniqueId = "PSP-${Math.abs(displayName.hashCode()) % 90000 + 10000}"
    val safeName = displayName.replace(Regex("[^A-Za-z0-9._ ()-]"), "_")
    val localFile = File(gamesDir, "${uniqueId}_$safeName")

    val copied = try {
      context.contentResolver.openInputStream(uri)?.use { input ->
        localFile.outputStream().use { output ->
          input.copyTo(output, bufferSize = 1 shl 20)
        }
      }
      localFile.exists() && localFile.length() > 0L
    } catch (_: Exception) {
      false
    }

    if (!copied) {
      localFile.delete()
      return null
    }

    val realSize = localFile.length()

    // Validación básica de cabecera (ISO9660/PBP/CSO/CHD).
    if (!PspArtwork.looksLikeGame(localFile)) {
      localFile.delete()
      return null
    }

    // Portada REAL desde el propio archivo del juego.
    val coverFile = File(coversDir, "$uniqueId.png")
    val coverBitmap = PspArtwork.extractIcon0(localFile)
    var coverPath: String? = null
    if (coverBitmap != null) {
      try {
        coverFile.outputStream().use { coverBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        coverPath = coverFile.absolutePath
      } catch (_: Exception) {
      }
      coverBitmap.recycle()
    }

    val extension = localFile.extension.uppercase(Locale.ROOT)
    val baseName = displayName.substringBeforeLast('.')
      .replace('_', ' ')
      .replace('-', ' ')
      .trim()

    val formattedSize = formatFileSize(realSize)
    val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val importedGame = PspGame(
      id = uniqueId,
      title = baseName.ifBlank { "Juego PSP" },
      subtitle = "$extension Title",
      genre = "Acción / Aventura",
      rating = "—",
      format = extension,
      size = formattedSize,
      coverResId = 0,
      coverPath = coverPath,
      localPath = localFile.absolutePath,
      filePath = uri.toString(),
      status = if (coverPath != null) CompatibilityStatus.PERFECT else CompatibilityStatus.UNVERIFIED,
      lastPlayed = currentDate,
      playTime = "0m",
      isFavorite = false,
      description = "Copia local: ${localFile.name} · Portada: ${if (coverPath != null) "ICON0 real del juego" else "no disponible"}",
      hasSavestate = false
    )

    val currentGames = getSavedGames().toMutableList()
    currentGames.removeAll { it.filePath == uri.toString() || it.id == importedGame.id }
    currentGames.add(0, importedGame)
    saveGames(currentGames)

    return importedGame
  }

  fun removeGame(gameId: String): List<PspGame> {
    val game = getSavedGames().firstOrNull { it.id == gameId }
    if (game != null) {
      // Borra también la copia local y la portada para no dejar basura.
      game.localPath?.let { path -> File(path).delete() }
      game.coverPath?.let { path -> File(path).delete() }
    }
    val updated = getSavedGames().filterNot { it.id == gameId }
    saveGames(updated)
    return updated
  }

  private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
      gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
      mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
      else -> String.format(Locale.US, "%.0f KB", kb)
    }
  }
}
