package com.diconium.android.codestyle

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

private const val TEST_FOLDER = "cachedDownloadTaskTest"

class CachedDownloadTaskTest {

    // used on the `getCacheFolder` tests
    private var originalUserHome: String? = null

    @Before
    fun setUp() {
        originalUserHome = System.getProperty(USER_HOME)
    }

    @After
    fun restore() {
        originalUserHome?.let { System.setProperty(USER_HOME, it) }
    }

    @Test
    fun getCacheFolder_returns_gradle_cache_folder_if_all_exist() {
        // prepare file system for test
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        val gradleFolder = File(testHome, CACHE_FOLDER)
        testHome.deleteOnExit()
        gradleFolder.deleteOnExit()
        gradleFolder.mkdirs()
        System.setProperty(USER_HOME, testHome.absolutePath)

        // test
        val tested = CachedDownloadTask.getCacheFolder()
        assertEquals(gradleFolder, tested)

        gradleFolder.delete()
        testHome.delete()

    }

    @Test
    fun getCacheFolder_returns_null_if_any_does_not_exist() {
        // prepare file system for test
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        testHome.deleteOnExit()
        testHome.mkdirs()

        // test 1 - no gradle folder
        System.setProperty(USER_HOME, testHome.absolutePath)
        assertNull(CachedDownloadTask.getCacheFolder())

        // test 2 - no user home folder
        testHome.delete()
        assertNull(CachedDownloadTask.getCacheFolder())

        // test 3 - no user home property
        System.clearProperty(USER_HOME)
        assertNull(CachedDownloadTask.getCacheFolder())

    }

    @Test
    fun string_validator_pass_for_valid_strings() {
        CachedDownloadTask.stringValidator("lorem ipsum")
    }

    @Test(expected = Exception::class)
    fun string_validator_throw_for_empty_string() {
        CachedDownloadTask.stringValidator("")
    }

    @Test(expected = Exception::class)
    fun string_validator_throw_for_blank_string() {
        CachedDownloadTask.stringValidator("  ")
    }

    @Test(expected = Exception::class)
    fun folder_validator_throw_for_not_a_folder() {
        val temp = Files.createTempFile("foo", "bar").toFile()
        temp.touch()
        CachedDownloadTask.folderValidator(temp)
        temp.delete()
    }

    @Test
    fun file_mover_moves_file() {
        // prepare file system for test
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        testHome.deleteOnExit()

        val source = File(testHome, "source")
        val content = "lorem ipsum"
        source.touch(content)

        val target = File(testHome, "target")

        assert(source.exists())
        assert(!target.exists())
        CachedDownloadTask.fileMover(source, target)
        assert(target.exists())
        assert(!source.exists())

        assertArrayEquals(content.toByteArray(), target.readBytes())

        source.delete()
        target.delete()

    }

    @Test
    fun file_copier_copies_file() {
        // prepare file system for test
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        testHome.deleteOnExit()

        val source = File(testHome, "source")
        val content = "lorem ipsum"
        source.touch(content)

        val target = File(testHome, "target")

        assert(source.exists())
        assert(!target.exists())
        CachedDownloadTask.fileCopier(source, target)
        assert(target.exists())
        assert(source.exists())

        assertArrayEquals(content.toByteArray(), source.readBytes())
        assertArrayEquals(content.toByteArray(), target.readBytes())

        source.delete()
        target.delete()

    }

    @Test
    fun compare_files_returns_true_equal_content_files() {
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        testHome.deleteOnExit()

        val content = "lorem ipsum"
        val f1 = File(testHome, "f1")
        f1.touch(content)
        val f2 = File(testHome, "f2")
        f2.touch(content)

        assertTrue(CachedDownloadTask.compareFiles(f1, f2))
    }


    @Test
    fun compare_files_returns_false_different_content_files() {
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        testHome.deleteOnExit()

        val f1 = File(testHome, "f1")
        f1.touch("lorem ipsum")
        val f2 = File(testHome, "f2")
        f2.touch("loremipsum")

        assertFalse(CachedDownloadTask.compareFiles(f1, f2))
    }

    // helper
    companion object {
         fun File.touch(content: String? = null) {
            val stream = FileOutputStream(this)
            content?.let { stream.write(it.toByteArray()) }
            stream.close()
            setLastModified(System.currentTimeMillis())

        }
    }
}