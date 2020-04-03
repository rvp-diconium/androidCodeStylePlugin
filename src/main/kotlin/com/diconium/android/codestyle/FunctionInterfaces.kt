package com.diconium.android.codestyle

import java.io.File

typealias StringValidator = (String) -> Unit
typealias FolderValidator = (File) -> Unit
typealias FileDownloader = (String, File) -> Unit
typealias FileCopier = (File, File) -> Unit
typealias CacheNameGenerator = (String, String) -> String
typealias FileMover = (File, File) -> Unit
typealias FileComparison = (File, File) -> Boolean
typealias Logger = (String) -> Unit