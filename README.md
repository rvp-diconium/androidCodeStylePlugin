# CodeStyle Plugin for Android Studio

Automatically applies the code style to the project.

It accomplishes that by copying (or downloading) the configured files into the project folder.  
The default is the internally supplied `Project.xml` and `codeStyleConfig.xml`.  
Using the `codeStyle {` closure, it can be configured to download those files from any online location.
 (e.g. a Github gist).  
 The files will be cached in the gradle cache folder.
 
See all the configuration parameters below.  

## Using

### From Maven
- add to the root build.gradle the buildscript Jitpack repository, dependencies and apply plugin
```
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.github.rvp-diconium:androidcCodeStylePlugin:1.0.0'
  }
}

apply plugin: "com.diconium.android.codestyle"
```

### From *.jar
- move the jar to libs on root
- add to the root build.gradle the buildscript dependencies and apply plugin
```
buildscript {
  dependencies {
    classpath fileTree(include: ['*.jar'], dir: 'libs')
    classpath files('codestyle.jar')
  }
}

apply plugin: "com.diconium.android.codestyle"
```

## Configuring

Configuration options are:
```
codeStyle {
    debug = true / false (default to false)
    maxCacheAge = 10 * 60 * 1000 (in millis) (default to 24h)
    force = true / false (default to false)
    useCache = true / false (default to true)
    downloadDir = "some/folder/path/" (defaults to .idea/codeStyle)
	downloads = [
			"Project.xml": "https://some/location/yourfile.xml"
	] (defaults to plugin internal code style)
}
```