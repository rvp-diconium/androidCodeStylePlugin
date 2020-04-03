package com.diconium.android.codestyle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
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

    @Before
    fun setUp() {
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

// https://stackoverflow.com/questions/60989927/test-custom-gradle-plugin-after-evaluate
//    @Test
//    fun task_is_created() {
//        val task = project.tasks.findByName("downloadCodeStyle")
//        assertNotNull(task)
//        assertTrue(task is CachedDownloadTask)
//    }
}