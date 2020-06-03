package com.diconium.android.codestyle

import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64

private const val BUFFER_SIZE = 10 * 1024
internal const val USER_HOME = "user.home"
internal const val MAX_CACHE_AGE = 1000L * 60 * 60 * 24
internal const val GRADLE_FOLDER = ".gradle"
internal const val CACHE_FOLDER = ".gradle/caches/modules-2/files-2.1/com.diconium.android.codestyle/filesCache/"

object Helpers {

  internal val stringValidator: StringValidator = { file ->
    if (file.isBlank()) {
      throw IllegalArgumentException("Invalid parameter $file")
    }
  }

  internal val folderValidator: FolderValidator = { folder ->
    if (!folder.exists()) {
      if (!folder.mkdirs()) {
        throw FileNotFoundException("Can't create ${folder.path}")
      }
    }
    if (!folder.isDirectory) {
      throw IllegalArgumentException("Not a directory ${folder.path}")
    }
  }

  internal val fileMover: FileMover = { from, to ->
    Files.move(
        from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
    )
  }

  internal val fileCopier: FileCopier = { from, to ->
    Files.copy(
        from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING
    )
  }

  internal val cacheNameGenerator: CacheNameGenerator = { sourceUrl, fileName ->
    val url = Base64.getEncoder()
        .encodeToString(sourceUrl.toByteArray())
    val path = Base64.getEncoder()
        .encodeToString(fileName.toByteArray())
    "$url-$path".replace(Regex("[^A-Za-z0-9]"), "_")
  }

  internal val md5Sum: (File) -> String = { file ->
    DigestUtils.md5Hex(FileInputStream(file))
  }

  internal val compareFiles: FileComparison = { f1, f2 ->
    if (f1.exists() && f2.exists()) {
      md5Sum(f1) == md5Sum(f2)
    } else {
      false
    }
  }

  internal val httpFileDownloader: FileDownloader = { uriString, destination ->
    val inputStream = safeUri(URI.create(uriString)).toURL()
        .openConnection()
        .getInputStream()
    val outputStream = BufferedOutputStream(FileOutputStream(destination))
    streamCopy(inputStream, outputStream)
  }

  internal val resourceFileDownloader: FileDownloader = { uriString, destination ->
    val inputStream = javaClass.getResourceAsStream(uriString)
    val outputStream = BufferedOutputStream(FileOutputStream(destination))
    streamCopy(inputStream, outputStream)
  }

  private fun streamCopy(
    inputStream: InputStream,
    outputStream: OutputStream
  ) {
    try {
      val buffer = ByteArray(BUFFER_SIZE)
      var numRead: Int
      while (inputStream.read(buffer)
              .also { numRead = it } != -1
      ) {
        if (Thread.currentThread().isInterrupted) {
          print("interrupted")
          throw IOException("Download was interrupted.")
        }
        outputStream.write(buffer, 0, numRead)
      }
    } finally {
      inputStream.close()
      outputStream.close()
    }
  }

  private fun safeUri(uri: URI): URI {
    return try {
      URI(uri.scheme, null, uri.host, uri.port, uri.path, uri.query, uri.fragment)
    } catch (e: URISyntaxException) {
      throw RuntimeException("Failed to parse URI", e)
    }
  }
}