package com.diconium.android.codestyle

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

private const val BUFFER_SIZE = 10 * 1024
internal const val MAX_CACHE_AGE = 1000L * 60 * 60 * 24
internal const val USER_HOME = "user.home"
internal const val GRADLE_FOLDER = ".gradle"
internal const val CACHE_FOLDER = ".gradle/caches/modules-2/files-2.1/com.diconium.android.codestyle/filesCache/"

open class CachedDownloadTask : DefaultTask() {

    @Input
    lateinit var downloads: Map<String, String>

    @Input
    var cacheDir: String? = null

    @Input
    var force: Boolean = false

    @Input
    var debug: Boolean = false

    @Input
    var maxCacheAge: Long = MAX_CACHE_AGE

    @OutputDirectory
    lateinit var outputDir: File

    @TaskAction
    fun execute() {
        CachedDownloadHandler.downloadNow(
            downloads,
            cacheDir?.let { File(it) },
            force,
            debug,
            maxCacheAge,
            outputDir
        )
    }

    companion object {

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
                from.toPath(),
                to.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        internal val fileCopier: FileCopier = { from, to ->
            Files.copy(
                from.toPath(),
                to.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        internal val cacheNameGenerator: CacheNameGenerator = { sourceUrl, fileName ->
            val url = Base64.getEncoder().encodeToString(sourceUrl.toByteArray())
            val path = Base64.getEncoder().encodeToString(fileName.toByteArray())
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

        internal val fileDownloader: FileDownloader = { urlString, destination ->
            var outputStream: OutputStream? = null
            val conn: URLConnection
            var inputStream: InputStream? = null
            try {
                val url = safeUri(URI.create(urlString)).toURL()
                outputStream = BufferedOutputStream(FileOutputStream(destination))
                conn = url.openConnection()
                inputStream = conn.getInputStream()
                val buffer = ByteArray(BUFFER_SIZE)
                var numRead: Int
                while (inputStream.read(buffer).also { numRead = it } != -1) {
                    if (Thread.currentThread().isInterrupted) {
                        print("interrupted")
                        throw IOException("Download was interrupted.")
                    }
                    outputStream.write(buffer, 0, numRead)
                }
            } finally {
                inputStream?.close()
                outputStream?.close()
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
}