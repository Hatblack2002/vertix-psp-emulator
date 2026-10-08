package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.R
import com.example.model.CompatibilityStatus
import com.example.model.PspGame
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(private val context: Context) {
  private val storageFile = File(context.filesDir, "vertix_library.json")

  fun getSavedGames(): List<PspGame> {
    if (!storageFile.exists()) return emptyList()
    return try {
      val jsonStr = storageFile.readText()
      val jsonArray = JSONArray(jsonStr)
      val list = mutableListOf<PspGame>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          PspGame(
            id = obj.optString("id", "GAME-$i"),
            title = obj.optString("title", "Juego desconocido"),
            subtitle = obj.optString("subtitle", "PSP Title"),
            genre = obj.optString("genre", "Acción"),
            rating = obj.optString("rating", "90%"),
            format = obj.optString("format", "ISO"),
            size = obj.optString("size", "0 MB"),
            coverResId = obj.optInt("coverResId", R.drawable.cover_gow),
            filePath = obj.optString("filePath", ""),
            status = try {
              CompatibilityStatus.valueOf(obj.optString("status", "PERFECT"))
            } catch (_: Exception) {
              CompatibilityStatus.PERFECT
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

    val extension = displayName.substringAfterLast('.', "iso").uppercase(Locale.ROOT)
    val baseName = displayName.substringBeforeLast('.')
      .replace('_', ' ')
      .replace('-', ' ')
      .trim()

    val formattedSize = formatFileSize(sizeBytes)
    val uniqueId = "PSP-${Math.abs(displayName.hashCode()) % 90000 + 10000}"

    val defaultCovers = listOf(R.drawable.cover_gow, R.drawable.cover_gta, R.drawable.cover_tekken)
    val chosenCover = defaultCovers[Math.abs(uniqueId.hashCode()) % defaultCovers.size]

    val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val importedGame = PspGame(
      id = uniqueId,
      title = baseName.ifBlank { "Juego PSP" },
      subtitle = "$extension Title",
      genre = "Acción / Aventura",
      rating = "90%",
      format = extension,
      size = formattedSize,
      coverResId = chosenCover,
      filePath = uri.toString(),
      status = CompatibilityStatus.PERFECT,
      lastPlayed = currentDate,
      playTime = "0m",
      isFavorite = false,
      description = "Archivo $displayName importado desde el almacenamiento del dispositivo.",
      hasSavestate = false
    )

    val currentGames = getSavedGames().toMutableList()
    currentGames.removeAll { it.filePath == uri.toString() || it.id == importedGame.id }
    currentGames.add(0, importedGame)
    saveGames(currentGames)

    return importedGame
  }

  fun removeGame(gameId: String): List<PspGame> {
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
