package com.diconium.android.codestyle

private const val DEFAULT_URL = "https://raw.githubusercontent.com/budius/ChromecastConverter/master/README.md"

open class CodeStyleConfig {
    var downloadUrl: String = DEFAULT_URL
    var useCache: Boolean = true
    var force: Boolean = false
}