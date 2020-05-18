package com.diconium.android.codestyle

open class CodeStyleConfig {
    var downloads: Map<String, String> = mapOf(
        "codeStyleConfig.xml" to "/codeStyles/codeStyleConfig.xml",
        "Project.xml" to "/codeStyles/Project.xml"
    )
    var downloadDir: String = ""
    var useCache: Boolean = false
    var force: Boolean = false
    var maxCacheAge: Long = MAX_CACHE_AGE
    var debug = false
}