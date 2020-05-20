package com.diconium.android.codestyle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CachedDownloadTask : DefaultTask() {

	@Input lateinit var downloads: Map<String, String>

	@Input var cacheDir: String? = null

	@Input var force: Boolean = false

	@Input var debug: Boolean = false

	@Input var maxCacheAge: Long = MAX_CACHE_AGE

	@OutputDirectory lateinit var outputDir: File

	@TaskAction fun execute() {
		CachedDownloader.downloadNow(
			downloads, cacheDir?.let { File(it) }, force, debug, maxCacheAge, outputDir
		)
	}
}