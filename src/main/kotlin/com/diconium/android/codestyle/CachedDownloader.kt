package com.diconium.android.codestyle

import java.io.File
import kotlin.system.measureTimeMillis

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

        private val defaultDownloads = mapOf(
            "codeStyleConfig.xml" to "/codeStyles/codeStyleConfig.xml",
            "Project.xml" to "/codeStyles/Project.xml"
        )

        internal fun downloadNow(
            downloadsInput: Map<String, String>,
            cacheDirInput: File?,
            force: Boolean,
            debug: Boolean,
            maxCacheAge: Long,
            outputDir: File
        ) {
            val logger = getLogger(debug)
            val (downloads, cacheDir) = prepareInputs(downloadsInput, cacheDirInput, logger)
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
                logger,
                outputDir,
                cacheDir
            )
            
            val executionTime = measureTimeMillis {
                downloads.forEach(downloader::execute)
            }
            logger("codeStyle download executed in ${executionTime}ms");
        }

        internal fun prepareInputs(
            downloadsInput: Map<String, String>,
            cacheDirInput: File?,
            logger: Logger
        ): Pair<Map<String, String>, File?> {

            // sets downloads map and cacheDir depending on downloads input
            return if (downloadsInput.isEmpty()) {
                logger("Starting codeStyle sync with plugin internal values")
                Pair(defaultDownloads, null)
            } else {
                downloadsInput.values.forEach {
                    if (!it.startsWith("http")) {
                        throw IllegalArgumentException("Not a valid HTTP URL: $it")
                    }
                }
                if (cacheDirInput == null) {
                    logger("Starting codeStyle download without cache")
                } else {
                    logger("Starting codeStyle download with cache")
                }
                Pair(downloadsInput, cacheDirInput)
            }
        }

        internal fun getLogger(debug: Boolean): Logger {
            return if (debug) ::println else ({})
        }
    }
}