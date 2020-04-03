package com.diconium.android.codestyle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class CodeStylePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val config = target.extensions.create("codestyle", CodeStyleConfig::class.java)
        target.afterEvaluate {
            val ideaFolder = File(target.rootProject.rootDir, ".idea/")

            target.tasks.create(
                "downloadCodeStyle",
                CachedDownloadTask::class.java
            ) {
                group = "build setup"
                sourceUrl = config.downloadUrl
                fileName = "test.dat"
                useCache = config.useCache
                force = config.force
                outputDir = ideaFolder
                maxCacheAge = config.maxCacheAge

                if (force) {
                    outputs.upToDateWhen {
                        false
                    }
                }
            }
        }
    }
}