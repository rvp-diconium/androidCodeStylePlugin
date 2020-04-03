buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("com.diconium.android.codestyle:codestyle:0.1.0-SNAPSHOT")
    }
}

apply {
    plugin("com.vanniktech.maven.publish")
    plugin("com.diconium.android.codestyle")
}

plugins {
    `kotlin-dsl`
    jacoco
}

extensions.configure<com.diconium.android.codestyle.CodeStyleConfig>("codestyle") {
    downloads = mapOf(
        "readme.txt" to "https://raw.githubusercontent.com/budius/ChromecastConverter/master/README.md",
        "main.java.txt" to "https://raw.githubusercontent.com/budius/ChromecastConverter/master/main/src/main/java/com/budius/chromecast/converter/Main.java"
    )
    //downloadDir = "$rootDir/temp_downloads/"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("commons-codec:commons-codec:1.14")
    testImplementation("junit:junit:4.13")
}

tasks.create("cleanBuildTestJacoco") {
    group = "build"
    dependsOn("clean")
    finalizedBy(
        "assemble",
        "test",
        "build",
        "jacocoTestReport"
    )
}