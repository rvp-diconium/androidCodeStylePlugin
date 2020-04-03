buildscript {
    repositories {
        mavenCentral()
        // for local testing:
        mavenLocal()
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        // for local testing:
        classpath("com.diconium.android.codestyle:codestyle:0.1.0-SNAPSHOT")
    }
}

apply {
    plugin("com.vanniktech.maven.publish")
    // for local testing:
    plugin("com.diconium.android.codestyle")
}

plugins {
    `kotlin-dsl`
    jacoco
}

// for local testing:
extensions.configure<com.diconium.android.codestyle.CodeStyleConfig>("codestyle") {
    downloads = mapOf(
        "codeStyles/readme.txt" to "https://raw.githubusercontent.com/budius/ChromecastConverter/master/README.md",
        "main.java.txt" to "https://raw.githubusercontent.com/budius/ChromecastConverter/master/main/src/main/java/com/budius/chromecast/converter/Main.java"
    )
    downloadDir = "$rootDir/.idea"
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