package com.diconium.android.codestyle

import com.diconium.android.codestyle.CachedDownloadTaskTest.Companion.touch
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import java.io.File
import java.nio.file.Files

/*
    Test matrix:
    (duplicate for target file already exist and not)
    (and some have extra for same content and different content)
    cache	|   force	|   cache status | done
    --------------------------------------------
    no  	|   yes	    |   -               v
    no	    |   no	    |   -               v
    yes	    |   yes 	|   old             v
    yes	    |   no	    |   old             v
    yes	    |   yes	    |   available       v
    yes    	|   no	    |   available       v
    yes     |   yes	    |   empty           v
    yes	    |   no	    |   empty           v
 */
private const val TEST_FOLDER = "cachedDownloadHandlerTest"

private const val DOWNLOAD_URL = "http://doenst/matter"
private const val FILE_NAME = "downloaded.txt"
private const val DOWNLOAD_CONTENTS = "lorem ipsum"
private const val OLD_CONTENTS = "old"
private const val CACHE_FILE_NAME = "cachefile"

class CachedDownloadHandlerTest {

    private lateinit var testHomeDir: File
    private lateinit var cacheDir: File
    private lateinit var outputDir: File
    private lateinit var remoteFile: File
    private lateinit var targetFile: File
    private lateinit var cacheFile: File

    @Before
    fun setUp() {
        fun createFolder(f: File): File {
            f.mkdirs()
            f.deleteOnExit()
            return f
        }

        testHomeDir = createFolder(Files.createTempDirectory(TEST_FOLDER).toFile())
        cacheDir = createFolder(File(testHomeDir, "cache"))
        outputDir = createFolder(File(testHomeDir, "output"))
        targetFile = File(outputDir, FILE_NAME)

        remoteFile = File(testHomeDir, "test.txt")
        remoteFile.touch(DOWNLOAD_CONTENTS)
        remoteFile.setLastModified(0)

        cacheFile = File(cacheDir, CACHE_FILE_NAME)

    }

    @After
    fun tearDown() {
        testHomeDir.deleteRecursively()
    }

    private fun createTestUnit(force: Boolean): CachedDownloadHandler {
        val fileDownloader: FileDownloader = { _, target ->
            // don't really download, just copy over
            CachedDownloadTask.fileCopier(remoteFile, target)
            target.setLastModified(System.currentTimeMillis() + 2000)
        }

        val fileCopier: FileCopier = { source, target ->
            //force copied files to have different time stamps
            // this way we can assert that the file was overwritten
            CachedDownloadTask.fileCopier(source, target)
            target.setLastModified(System.currentTimeMillis() + 2000)
        }
        val cacheNameGenerator: CacheNameGenerator = { _, _ -> CACHE_FILE_NAME }
        return CachedDownloadHandler(
            CachedDownloadTask.stringValidator,
            CachedDownloadTask.folderValidator,
            cacheNameGenerator,
            fileDownloader,
            fileCopier,
            CachedDownloadTask.fileMover,
            CachedDownloadTask.compareFiles,
            force,
            MAX_CACHE_AGE,
            ({})
        )
    }

    @Test
    fun no_cache_force_target_downloaded() {
        targetFile.touch(DOWNLOAD_CONTENTS)
        val targetModified = targetFile.lastModified()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, null)
        assertEquals(remoteFile, targetFile)
        assertNotEquals(targetModified, targetFile.lastModified())
    }

    @Test
    fun no_cache_force_no_target() {
        targetFile.delete()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, null)
        assertEquals(remoteFile, targetFile)
    }

    @Test
    fun no_cache_no_force_target_downloaded_different_content() {
        targetFile.touch(OLD_CONTENTS)
        val targetModified = targetFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, null)
        assertEquals(remoteFile, targetFile)
        assertNotEquals(targetModified, targetFile)
    }

    @Test
    fun no_cache_no_force_target_downloaded_same_content() {
        targetFile.touch(DOWNLOAD_CONTENTS)
        val targetModified = targetFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, null)
        assertEquals(remoteFile, targetFile)
        assertEquals(targetModified, targetFile.lastModified())
    }

    @Test
    fun no_cache_no_force_no_target() {
        targetFile.delete()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, null)
        assertEquals(remoteFile, targetFile)
    }

    @Test
    fun use_cache_force_old_target() {
        targetFile.touch(DOWNLOAD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.touch(DOWNLOAD_CONTENTS)
        cacheFile.setLastModified(oldAge())
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(targetModified, targetFile.lastModified())
        assertNotEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_force_old_no_target() {
        targetFile.delete()
        cacheFile.touch(DOWNLOAD_CONTENTS)
        cacheFile.setLastModified(oldAge())
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_no_force_old_target_same_content() {
        targetFile.touch(DOWNLOAD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.touch(DOWNLOAD_CONTENTS)
        cacheFile.setLastModified(oldAge())
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertEquals(targetModified, targetFile.lastModified())
        assertNotEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_no_force_old_target_different_content() {
        targetFile.touch(OLD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.touch(DOWNLOAD_CONTENTS)
        cacheFile.setLastModified(oldAge())
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(targetModified, targetFile.lastModified())
        assertNotEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_no_force_old_no_target() {
        targetFile.delete()
        cacheFile.touch(DOWNLOAD_CONTENTS)
        cacheFile.setLastModified(oldAge())
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_force_cached_available() {
        targetFile.touch(OLD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.touch(OLD_CONTENTS)
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(targetModified, targetFile.lastModified())
        assertNotEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_no_force_cached_available() {
        targetFile.touch(OLD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.touch(OLD_CONTENTS)
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(cacheFile, targetFile)
        assertNotEquals(remoteFile, cacheFile)
        assertEquals(targetModified, targetFile.lastModified())
        assertEquals(cacheModified, cacheFile.lastModified())
    }

    @Test
    fun use_cache_no_force_cached_available_someone_manually_edit_target() {
        targetFile.touch(OLD_CONTENTS + "lorem ipsum part 2z")
        val targetModified = targetFile.lastModified()
        cacheFile.touch(OLD_CONTENTS)
        val cacheModified = cacheFile.lastModified()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(cacheFile, targetFile)
        assertNotEquals(remoteFile, cacheFile)
        assertNotEquals(targetModified, targetFile.lastModified())
        assertEquals(cacheModified, cacheFile.lastModified())
    }

    ////////////////===============


    @Test
    fun use_cache_force_empty_target() {
        targetFile.touch(DOWNLOAD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.delete()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(targetModified, targetFile.lastModified())
    }

    @Test
    fun use_cache_force_empty_no_target() {
        targetFile.delete()
        cacheFile.delete()
        val tested = createTestUnit(true)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
    }

    @Test
    fun use_cache_no_force_empty_target_same_content() {
        targetFile.touch(DOWNLOAD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.delete()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertEquals(targetModified, targetFile.lastModified())
    }

    @Test
    fun use_cache_no_force_empty_target_different_content() {
        targetFile.touch(OLD_CONTENTS)
        val targetModified = targetFile.lastModified()
        cacheFile.delete()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
        assertNotEquals(targetModified, targetFile.lastModified())
    }

    @Test
    fun use_cache_no_force_empty_no_target() {
        targetFile.delete()
        cacheFile.delete()
        val tested = createTestUnit(false)
        tested.execute(DOWNLOAD_URL, FILE_NAME, outputDir, cacheDir)
        assertEquals(remoteFile, targetFile)
        assertEquals(remoteFile, cacheFile)
    }

    // helpers
    companion object {

        private fun assertEquals(f1: File, f2: File) {
            assertTrue(CachedDownloadTask.compareFiles(f1, f2))
        }

        private fun assertNotEquals(f1: File, f2: File) {
            assertFalse(CachedDownloadTask.compareFiles(f1, f2))
        }

        private fun oldAge(): Long {
            return System.currentTimeMillis() - MAX_CACHE_AGE - 10000
        }
    }
}