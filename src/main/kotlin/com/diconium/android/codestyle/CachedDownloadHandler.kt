package com.diconium.android.codestyle

import java.io.File

typealias StringValidator = (String) -> Unit
typealias FolderValidator = (File) -> Unit
typealias FileDownloader = (String, File) -> Unit
typealias FileCopier = (File, File) -> Unit
typealias CacheNameGenerator = (String, String) -> String
typealias FileMover = (File, File) -> Unit
typealias FileComparison = (File, File) -> Boolean

class CachedDownloadHandler(
    private val validateString: StringValidator,
    private val validateFolder: FolderValidator,
    private val generateCacheFileName: CacheNameGenerator,
    private val downloadFile: FileDownloader,
    private val copyFile: FileCopier,
    private val moveFile: FileMover,
    private val isTheSame: FileComparison,
    private val forceOverwrite: Boolean
) {
    internal fun execute(
        sourceUrl: String,
        fileName: String,
        outputDir: File,
        cacheDir: File?
    ) {

        // validate inputs
        validateInputs(sourceUrl, fileName, outputDir, cacheDir)
        val destinationFile = File(outputDir, fileName)
        var tempFile: File? = null
        var maybeException: Exception? = null
        try {
            if (cacheDir != null) {
                // runs with cache
                val cacheFileName = generateCacheFileName(sourceUrl, fileName)
                val cacheFile = File(cacheDir, cacheFileName)
                tempFile = File(cacheDir, "$cacheFileName.tmp")
                tempFile.deleteOnExit()
                executeWithCache(sourceUrl, destinationFile, tempFile, cacheFile)
            } else {
                // runs without cache
                tempFile = File(outputDir, "$fileName.tmp")
                tempFile.deleteOnExit()
                executeWithoutCache(sourceUrl, destinationFile, tempFile)
            }
        } catch (e: Exception) {
            println("Downloader execution failed. ${e.message}")
            if (forceOverwrite) {
                throw e
            }
            maybeException = e
        } finally {
            tempFile?.delete()
        }

        // only fails the task with the destination file does not exist
        if (!destinationFile.exists()) {
            throw maybeException ?: IllegalStateException("Downloader execution failed with unknown error")
        }
    }

    private fun executeWithCache(
        sourceUrl: String,
        destinationFile: File,
        tempFile: File,
        cacheFile: File
    ) {

        println("Downloading from $sourceUrl to ${tempFile.path}")
        downloadFile(sourceUrl, tempFile)

        // move temp to cache
        println("Moving downloaded file to cache")
        moveFile(tempFile, cacheFile)

        if (forceOverwrite || !isTheSame(cacheFile, destinationFile)) {
            println("Copying file from ${cacheFile.path} to ${destinationFile.path}")
            copyFile(cacheFile, destinationFile)
        }
    }

    private fun executeWithoutCache(
        sourceUrl: String,
        destinationFile: File,
        tempFile: File
    ) {

        println("Downloading from $sourceUrl to ${tempFile.path}")
        downloadFile(sourceUrl, tempFile)

        if (forceOverwrite || !isTheSame(tempFile, destinationFile)) {
            println("Moving file from ${tempFile.path} to ${destinationFile.path}")
            moveFile(tempFile, destinationFile)
        }
    }

    private fun validateInputs(
        sourceUrl: String,
        fileName: String,
        outputDir: File,
        cacheDir: File?
    ) {
        validateString(sourceUrl)
        validateString(fileName)
        validateFolder(outputDir)
        cacheDir?.let { validateFolder(it) }
    }

}