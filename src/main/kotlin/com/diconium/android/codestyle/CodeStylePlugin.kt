package com.diconium.android.codestyle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

const val EXTENSION_NAME = "codeStyle"

class CodeStylePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val config = target.extensions.create(EXTENSION_NAME, CodeStyleConfig::class.java)
        target.afterEvaluate {

            val cache = getCacheFolder(config.useCache)

            // this executes every time the project is evaluated
            // so here we ignore `force` flag, uses cache
            CachedDownloader.downloadNow(
                config.downloads,
                cache,
                false,
                config.debug,
                config.maxCacheAge,
                getDownloadDir(target, config)
            )

            // task is added to the gradle tasks
            // and can be executed manually
            target.tasks.create(
                "downloadCodeStyle",
                CachedDownloadTask::class.java
            ) {
                group = "help"
                downloads = config.downloads
                cacheDir = cache?.absolutePath
                force = config.force
                outputDir = getDownloadDir(target, config)
                maxCacheAge = config.maxCacheAge

                if (config.force) {
                    outputs.upToDateWhen {
                        false
                    }
                }
            }
        }
    }

    companion object {

        internal fun getDownloadDir(target: Project, config: CodeStyleConfig): File {
            return if (config.downloadDir.isBlank()) {
                File(target.rootProject.rootDir, ".idea/codeStyles").also {
                    // when using .idea/codeStyle
                    // we'll be sure to auto-create if needed
                    it.mkdirs()
                    if (!it.isDirectory) {
                        throw IllegalStateException("Cannot instantiate codeStyle folder ${it.path}")
                    }
                }
            } else {
                File(config.downloadDir).also {
                    // when receiving externally,
                    // config must supply a valid directory
                    if (!it.isDirectory) {
                        throw IllegalArgumentException("downloadDir must be a valid directory. ${config.downloadDir}")
                    }
                }
            }
        }


        internal fun getCacheFolder(useCache: Boolean): File? {
            if (!useCache) {
                return null
            }

            val userHomePath = System.getProperty(USER_HOME)
            if (userHomePath.isNullOrBlank()) {
                return null
            }

            val userHome = File(userHomePath)
            if (!userHome.exists()) {
                return null
            }

            val gradleFolder = File(userHome, GRADLE_FOLDER)
            if (!gradleFolder.exists()) {
                return null
            }

            return File(userHome, CACHE_FOLDER)
        }
    }
}