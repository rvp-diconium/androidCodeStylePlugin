package com.diconium.android.codestyle

open class CodeStyleConfig {
    var downloads: Map<String, String> = emptyMap()
    var downloadDir: String = ""
    var useCache: Boolean = true
    var force: Boolean = false
    var maxCacheAge: Long = MAX_CACHE_AGE
    var debug = false
}