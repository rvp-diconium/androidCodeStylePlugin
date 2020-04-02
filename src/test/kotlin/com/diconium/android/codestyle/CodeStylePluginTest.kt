package com.diconium.android.codestyle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CodeStylePluginTest {

    private lateinit var project: Project

    @Before
    fun setUp() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.diconium.android.codestyle")
    }

    @Test
    fun extension_config_is_created() {
        val codestyle = project.extensions.findByName("codestyle")
        assertNotNull(codestyle)
        assertTrue(codestyle is CodeStyleConfig)
    }
//
//    @Test
//    fun task_is_created() {
//        val task = project.tasks.findByName("downloadCodeStyle")
//        assertNotNull(task)
//        assertTrue(task is CachedDownloadTask)
//    }
}