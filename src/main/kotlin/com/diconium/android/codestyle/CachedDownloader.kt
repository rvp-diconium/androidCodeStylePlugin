package com.diconium.android.codestyle

import java.io.File

class CachedDownloader(
    private val validateString: StringValidator,
    private val validateFolder: FolderValidator,
    private val generateCacheFileName: CacheNameGenerator,
    private val downloadFile: FileDownloader,
    private val copyFile: FileCopier,
    private val moveFile: FileMover,
    private val isTheSame: FileComparison,
    private val forceOverwrite: Boolean,
    private val maxCacheAge: Long,
    private val log: Logger,
    private val outputDir: File,
    private val cacheDir: File?
) {
    internal fun execute(fileName: String, sourceUrl: String) {

        // validate inputs
        validateInputs(sourceUrl, fileName, outputDir, cacheDir)
        val destinationFile = File(outputDir, fileName)
        var tempFile: File? = null
        var maybeException: Exception? = null
        try {
            if (cacheDir != null) {
                // runs with cache
                log("Running downloader with cache")
                val cacheFileName = generateCacheFileName(sourceUrl, fileName)
                val cacheFile = File(cacheDir, cacheFileName)
                tempFile = File(cacheDir, "$cacheFileName.tmp")
                tempFile.deleteOnExit()
                executeWithCache(sourceUrl, destinationFile, tempFile, cacheFile)
            } else {
                // runs without cache
                log("Running downloader without cache")
                tempFile = File(outputDir, "$fileName.tmp")
                tempFile.deleteOnExit()
                executeWithoutCache(sourceUrl, destinationFile, tempFile)
            }
        } catch (e: Exception) {
            if (forceOverwrite) {
                throw e
            }
            log("File download failed. ${e.message}.")
            maybeException = e
        } finally {
            tempFile?.delete()
        }

        // fails the task if the destination file does not exist
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

        if (needsUpdateCache(cacheFile)) {
            log("Downloading from $sourceUrl to ${tempFile.path}")
            downloadFile(sourceUrl, tempFile)

            // move temp to cache
            log("Moving downloaded file to cache")
            moveFile(tempFile, cacheFile)
        } else {
            log("Cache file is up-to-date")
        }

        if (needsUpdateTarget(cacheFile, destinationFile)) {
            log("Copying file from ${cacheFile.path} to ${destinationFile.path}")
            copyFile(cacheFile, destinationFile)
        } else {
            log("File at destination up-to-date with cache")
        }
    }

    private fun needsUpdateCache(cached: File): Boolean {
        return forceOverwrite
                || !cached.exists()
                || isOldFile(cached)
    }

    private fun needsUpdateTarget(from: File, target: File): Boolean {
        return forceOverwrite
                || !target.exists()
                || !isTheSame(from, target)
    }


    private fun isOldFile(file: File): Boolean {
        return (System.currentTimeMillis() - file.lastModified() > maxCacheAge)
    }

    private fun executeWithoutCache(
        sourceUrl: String,
        destinationFile: File,
        tempFile: File
    ) {

        log("Downloading from $sourceUrl to ${tempFile.path}")
        downloadFile(sourceUrl, tempFile)

        if (needsUpdateTarget(tempFile, destinationFile)) {
            log("Moving file from ${tempFile.path} to ${destinationFile.path}")
            moveFile(tempFile, destinationFile)
        } else {
            log("File at destination up-to-date with source")
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

    companion object {
        internal fun downloadNow(
            downloads: Map<String, String>,
            cacheDir: File?,
            force: Boolean,
            debug: Boolean,
            maxCacheAge: Long,
            outputDir: File
        ) {
            val downloader = CachedDownloader(
                Helpers.stringValidator,
                Helpers.folderValidator,
                Helpers.cacheNameGenerator,
                Helpers.fileDownloader,
                Helpers.fileCopier,
                Helpers.fileMover,
                Helpers.compareFiles,
                force,
                maxCacheAge,
                if (debug) ::println else ({}),
                outputDir,
                cacheDir
            )
            downloads.forEach(downloader::execute)
        }
    }

}