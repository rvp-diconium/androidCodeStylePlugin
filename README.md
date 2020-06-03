# CodeStyle Plugin for Android Studio

Automatically applies the code style to the project.

## Using

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

### From Maven
TODO:

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