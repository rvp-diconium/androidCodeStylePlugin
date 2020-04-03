package com.diconium.android.codestyle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class CodeStylePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val config = target.extensions.create("codestyle", CodeStyleConfig::class.java)
        target.afterEvaluate {

            // this executes every time the project is evaluated
            // so here we ignore `force` flag, uses cache
            CachedDownloadHandler.downloadNow(
                config.downloads,
                config.useCache,
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
                useCache = config.useCache
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
    }
}