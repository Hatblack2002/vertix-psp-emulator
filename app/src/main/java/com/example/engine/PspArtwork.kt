package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.DataInputStream
import java.io.File
import java.io.RandomAccessFile

/**
 * Extracción REAL de la portada de un juego PSP desde su propio archivo:
 *
 *  * ISO9660 (.iso): localiza /PSP_GAME/ICON0.PNG recorriendo el sistema de
 *    archivos UMD (sectores de 2048 bytes, PVD en el sector 16).
 *  * PBP (.pbp): el header PBP contiene los offsets directos de ICON0.PNG.
 *  * CSO (.cso): imagen comprimida; la portada se extraería tras
 *    descomprimir todo el archivo, así que se devuelve null y la UI muestra
 *    un marcador neutral (sin portadas falsas).
 *
 * Devuelve el Bitmap decodificado o null si no se encuentra.
 */
object PspArtwork {

  private const val SECTOR = 2048

  fun extractIcon0(gameFile: File): Bitmap? {
    return try {
      when (gameFile.extension.lowercase()) {
        "iso" -> extractFromIso(gameFile)
        "pbp" -> extractFromPbp(gameFile)
        else -> null
      }
    } catch (_: Throwable) {
      null
    }
  }

  // -----------------------------------------------------------------------
  // PBP: magic "\0PBP" + version + 8 offsets (PARAM.SFO, ICON0.PNG,
  // ICON1.PMF, UNKNOWN, PIC0, PIC1, SND0, PSP_DATA).
  // -----------------------------------------------------------------------
  private fun extractFromPbp(file: File): Bitmap? {
    RandomAccessFile(file, "r").use { raf ->
      val magic = ByteArray(4)
      raf.readFully(magic)
      if (magic[1] != 'P'.code.toByte() || magic[2] != 'B'.code.toByte() || magic[3] != 'P'.code.toByte()) {
        return null
      }
      raf.skipBytes(4) // versión
      val offsets = LongArray(8)
      for (i in 0 until 8) offsets[i] = Integer.reverseBytes(raf.readInt()).toLong() and 0xFFFFFFFFL
      val start = offsets[1]
      val end = if (offsets[2] > start) offsets[2] else file.length()
      if (start <= 0L || end <= start) return null
      val bytes = ByteArray((end - start).toInt())
      raf.seek(start)
      raf.readFully(bytes)
      return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
  }

  // -----------------------------------------------------------------------
  // ISO9660: PVD (sector 16) -> root directory record -> búsqueda de
  // PSP_GAME/ICON0.PNG.
  // -----------------------------------------------------------------------
  private fun extractFromIso(file: File): Bitmap? {
    RandomAccessFile(file, "r").use { raf ->
      // Volume Descriptor Set Terminator check + PVD
      val pvd = readSector(raf, 16) ?: return null
      if (pvd[1] != 'C'.code.toByte() || pvd[2] != 'D'.code.toByte() ||
        pvd[3] != '0'.code.toByte() || pvd[4] != '0'.code.toByte() || pvd[5] != '1'.code.toByte()
      ) {
        return null
      }
      val root = parseDirRecord(pvd, 156) ?: return null

      // Busca el directorio PSP_GAME en la raíz
      val pspGame = findInDir(raf, root.extent, root.size, "PSP_GAME") ?: return null
      if (pspGame.size <= 0L) return null
      // Busca ICON0.PNG dentro de PSP_GAME
      val icon = findInDir(raf, pspGame.extent, pspGame.size, "ICON0.PNG") ?: return null
      if (icon.size <= 0L || icon.size > 4L * 1024 * 1024) return null

      raf.seek(icon.extent * SECTOR.toLong())
      val bytes = ByteArray(icon.size.toInt())
      raf.readFully(bytes)
      return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
  }

  private data class DirEntry(val extent: Long, val size: Long)

  private fun readSector(raf: RandomAccessFile, sector: Long): ByteArray? {
    val buffer = ByteArray(SECTOR)
    raf.seek(sector * SECTOR.toLong())
    val read = raf.read(buffer)
    return if (read == SECTOR) buffer else null
  }

  private fun parseDirRecord(sector: ByteArray, offset: Int): DirEntry? {
    if (offset + 34 > sector.size) return null
    val extent = readLE32(sector, offset + 2)
    val size = readLE32(sector, offset + 10)
    return DirEntry(extent, size)
  }

  private fun readLE32(b: ByteArray, off: Int): Long {
    return (b[off].toLong() and 0xFF) or
      ((b[off + 1].toLong() and 0xFF) shl 8) or
      ((b[off + 2].toLong() and 0xFF) shl 16) or
      ((b[off + 3].toLong() and 0xFF) shl 24)
  }

  /**
   * Recorre un directorio ISO9660 (que puede abarcar varios sectores) y
   * devuelve la entrada cuyo nombre (normalizado, sin ";1") coincida.
   */
  private fun findInDir(raf: RandomAccessFile, dirExtent: Long, dirSize: Long, wanted: String): DirEntry? {
    val total = dirSize.toInt()
    if (total <= 0 || total > 16 * 1024 * 1024) return null
    val data = ByteArray(total)
    raf.seek(dirExtent * SECTOR.toLong())
    raf.readFully(data)

    var pos = 0
    val target = wanted.uppercase()
    while (pos < total) {
      val len = data[pos].toInt() and 0xFF
      if (len == 0) {
        // Fin de sector: salta al siguiente sector
        val nextSector = ((pos / SECTOR) + 1) * SECTOR
        if (nextSector >= total) break
        pos = nextSector
        continue
      }
      val nameLen = data[pos + 32].toInt() and 0xFF
      if (nameLen > 0) {
        val rawName = String(data, pos + 33, nameLen, Charsets.US_ASCII)
        val name = rawName
          .substringBefore(';')
          .trimEnd('.')
          .uppercase()
        if (name == target) {
          return DirEntry(readLE32(data, pos + 2), readLE32(data, pos + 10))
        }
      }
      pos += len
    }
    return null
  }

  /** Devuelve true si el archivo parece una imagen PSP legible (cabecera básica). */
  fun looksLikeGame(file: File): Boolean {
    return try {
      DataInputStream(file.inputStream().buffered()).use { din ->
        val head = ByteArray(16)
        din.readFully(head)
        val ext = file.extension.lowercase()
        when (ext) {
          "iso" -> head[1] == 'C'.code.toByte() && head[2] == 'D'.code.toByte() && head[3] == '0'.code.toByte()
          "pbp" -> head[0] == 0.toByte() && head[1] == 'P'.code.toByte() && head[2] == 'B'.code.toByte() && head[3] == 'P'.code.toByte()
          "cso" -> head[0] == 'C'.code.toByte() && head[1] == 'S'.code.toByte() && head[2] == 'O'.code.toByte()
          "chd" -> head[0] == 'C'.code.toByte() && head[1] == 'h'.code.toByte() && head[2] == 'D'.code.toByte()
          else -> true // ELF y otros formatos que el núcleo sí arranca
        }
      }
    } catch (_: Throwable) {
      false
    }
  }
}
