# CodeStyle Plugin for Android Studio

Automatically applies the code style to the project.

## Using

### From *.jar
- move the jar to libs on root
- add to `buildscript.dependencies`: `classpath fileTree(include: ['*.jar'], dir: 'libs')`
- add to `buildscript.dependencies`: `classpath files('codestyle.jar')`
- apply the plugin: `apply plugin: "com.diconium.android.codestyle"`

### From Maven
TODO:

## Configuring

Configuration options are:
```
codeStyle {
    debug = true / false (default to false)
    maxCacheAge = 10 * 60 * 1000 (in millis) (default is 24h)
    force = true / false (default to false)
    useCache = true / false (default to false, 
                                because default is the internaly packed, 
                                so there's no need to cache it)
    downloadDir = "some/folder/path/" (defaults to .idea/codeStyle)
	downloads = [
			"Project.xml": "https://some/location/yourfile.xml"
	]
}
```