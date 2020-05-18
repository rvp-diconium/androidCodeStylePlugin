package com.diconium.android.codestyle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

private const val TEST_FOLDER = "codeStylePluginTest"

class CodeStylePluginTest {

    private lateinit var testHomeDir: File
    private lateinit var project: Project

    // used on the `getCacheFolder` tests
    private var originalUserHome: String? = null

    @Before
    fun setUp() {
        originalUserHome = System.getProperty(USER_HOME)
        fun createFolder(f: File): File {
            f.mkdirs()
            f.deleteOnExit()
            return f
        }
        testHomeDir = createFolder(Files.createTempDirectory(TEST_FOLDER).toFile())
        project = ProjectBuilder
            .builder()
            .withProjectDir(testHomeDir)
            .build()
        project.pluginManager.apply("com.diconium.android.codestyle")
    }

    @After
    fun tearDown() {
        originalUserHome?.let { System.setProperty(USER_HOME, it) }
        originalUserHome = null
        testHomeDir.deleteRecursively()
    }

    @Test
    fun extension_config_is_created() {
        val codestyle = project.extensions.findByName("codestyle")
        assertNotNull(codestyle)
        assertTrue(codestyle is CodeStyleConfig)
    }

    @Test
    fun download_dir_default_config_creates_idea_codeStyle_folder() {
        val config = CodeStyleConfig()
        CodeStylePlugin.getDownloadDir(project, config)
        val dir = File(testHomeDir, ".idea/codeStyles")
        assertTrue(dir.exists() && dir.isDirectory)
    }

    @Test
    fun download_dir_valid_folder_passes() {
        val dir = File(testHomeDir, "custom_location_1")
        val config = CodeStyleConfig().apply { downloadDir = dir.path }
        dir.mkdirs()
        CodeStylePlugin.getDownloadDir(project, config)
    }

    @Test(expected = Exception::class)
    fun download_dir_invalid_folder_throws() {
        val dir = File(testHomeDir, "custom_location_2")
        val config = CodeStyleConfig().apply { downloadDir = dir.path }
        CodeStylePlugin.getDownloadDir(project, config)
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
        val tested = CodeStylePlugin.getCacheFolder(true)
        Assert.assertEquals(gradleFolder, tested)

        gradleFolder.delete()
        testHome.delete()

    }

    @Test
    fun getCacheFolder_returns_null_if_useCache_is_false() {
        // prepare file system for test
        val testHome = Files.createTempDirectory(TEST_FOLDER).toFile()
        val gradleFolder = File(testHome, CACHE_FOLDER)
        testHome.deleteOnExit()
        gradleFolder.deleteOnExit()
        gradleFolder.mkdirs()
        System.setProperty(USER_HOME, testHome.absolutePath)

        // test
        val tested = CodeStylePlugin.getCacheFolder(false)
        Assert.assertNull(tested)

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
        Assert.assertNull(CodeStylePlugin.getCacheFolder(true))

        // test 2 - no user home folder
        testHome.delete()
        Assert.assertNull(CodeStylePlugin.getCacheFolder(true))

        // test 3 - no user home property
        System.clearProperty(USER_HOME)
        Assert.assertNull(CodeStylePlugin.getCacheFolder(true))

    }

// https://stackoverflow.com/questions/60989927/test-custom-gradle-plugin-after-evaluate
//    @Test
//    fun task_is_created() {
//        val task = project.tasks.findByName("downloadCodeStyle")
//        assertNotNull(task)
//        assertTrue(task is CachedDownloadTask)
//    }
}