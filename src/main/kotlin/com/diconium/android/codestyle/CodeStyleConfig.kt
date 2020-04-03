package com.diconium.android.codestyle

private const val DEFAULT_URL = "https://raw.githubusercontent.com/budius/ChromecastConverter/master/README.md"

open class CodeStyleConfig {
    var downloads: Map<String, String> = mapOf("temp.dat" to DEFAULT_URL)
    var downloadDir: String = ""
    var useCache: Boolean = true
    var force: Boolean = false
    var maxCacheAge: Long = MAX_CACHE_AGE
    var debug = true
}